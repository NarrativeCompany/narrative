package org.narrative.common.persistence.hibernate;

import org.narrative.common.cache.CacheManager;
import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.hibernate.integrator.MetadataExtractorIntegrator;
import org.narrative.common.util.Debug;
import org.narrative.common.util.NarrativeLogger;
import org.narrative.common.util.UnexpectedError;
import org.narrative.config.StaticConfig;
import org.narrative.config.cache.hibernate.ManagedRedissonLocalCachedRegionFactory;
import org.narrative.config.properties.NarrativeProperties;
import org.narrative.network.core.cluster.partition.PartitionType;
import org.narrative.network.shared.services.NetworkAnnotationConfiguration;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Environment;
import org.hibernate.event.internal.DefaultEvictEventListener;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.EvictEvent;
import org.hibernate.event.spi.EvictEventListener;
import org.hibernate.event.spi.PostDeleteEvent;
import org.hibernate.event.spi.PostDeleteEventListener;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.event.spi.PostLoadEvent;
import org.hibernate.event.spi.PostLoadEventListener;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.hibernate.event.spi.PreDeleteEvent;
import org.hibernate.event.spi.PreDeleteEventListener;
import org.hibernate.event.spi.PreInsertEvent;
import org.hibernate.event.spi.PreInsertEventListener;
import org.hibernate.event.spi.PreLoadEvent;
import org.hibernate.event.spi.PreLoadEventListener;
import org.hibernate.event.spi.PreUpdateEvent;
import org.hibernate.event.spi.PreUpdateEventListener;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.proxy.LazyInitializer;
import org.hibernate.resource.jdbc.spi.PhysicalConnectionHandlingMode;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.BasicType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Nov 27, 2005
 * Time: 11:31:07 PM
 */
public class GSessionFactory {

    private static final NarrativeLogger logger = new NarrativeLogger(GSessionFactory.class);

    private static Properties extraProperties = new Properties();
    private ThreadLocal<GSession> currentSession = new ThreadLocal<>();
    private final PartitionType partitionType;
    private final String name;
    private MetadataExtractorIntegrator metadataExtractorIntegrator;

    /**
     * this method can be used to enable SQL logging for Hibernate queries
     *
     * @param enable true if SQL logging should be enabled for Hibernate queries.
     *               bfalse if SQL logging should be disabled.
     */
    public static void setEnableSQLLogging(boolean enable) {
        // if SQL logging is enabled, then set the show_sql environment variable
        extraProperties.setProperty(Environment.SHOW_SQL, Boolean.toString(enable));
    }

    public static void setEnableHibernateStatistics(boolean enable) {
        extraProperties.setProperty(Environment.GENERATE_STATISTICS, Boolean.toString(enable));
    }

    private SessionFactory sessionFactory = null;

    //the hibernate default event listeners that we have overridden
    private DefaultEvictEventListener evictEventListener = new DefaultEvictEventListener();
    private Map<EventListener, EventListener> globalListeners = new ConcurrentHashMap<>();
    private Map<Class, Set<EventListener>> classListeners = new ConcurrentHashMap<>();

    /**
     * Registers an event listener for all classes
     *
     * @param listener
     */
    public synchronized void registerGlobalEventListener(EventListener listener) {
        Debug.assertMsg(logger, globalListeners != null, "You can only register event listeners before hibernate has been inited");
        globalListeners.put(listener, listener);
    }

    /**
     * Registers an event listener for a specified list of classes
     *
     * @param listener
     * @param classes
     */
    public synchronized void registerClassEventListener(EventListener listener, Class... classes) {
        Debug.assertMsg(logger, classListeners != null, "You can only register event listeners before hibernate has been inited");
        for (Class cls : classes) {
            Set<EventListener> listeners = classListeners.get(cls);
            if (listeners == null) {
                listeners = new HashSet<>();
                classListeners.put(cls, listeners);
            }

            listeners.add(listener);
        }
    }

    /**
     * This method opens a session and adds it to the stack of sessions on thread local storage.
     * Never needs to be used if you use DBSetManager or PlatformContext.
     *
     * @return The opened session.
     */
    public GSession openSession(OID partitionOid) {
        Debug.assertMsg(logger, sessionFactory != null, "GSessionFactory must be configured before calling this method");
        return new GSession(this, partitionOid);

    }

    /**
     * This closes the current session found at the top of thread local and pops it off the stack.
     * Never needs to be used if you use DBSetManager or PlatformContext.
     */
    public void closeSession(GSession session) {
        if (session == null) {
            return;
        }

        session.close();
    }

    /**
     * Loads a hibernate configuration based on a config set.  Used by DBSetManager.  Shouldn't
     * ever need to be called externally.
     */
    public GSessionFactory(PartitionType partitionType) {
        this.partitionType = partitionType;
        this.name = partitionType.name();
    }

    public void init(NetworkAnnotationConfiguration cfg) {
        Debug.assertMsg(logger, sessionFactory == null, "configure() can only be called once.  Init was already done.");

        //add the config set as a cache region to allow for multiple databases to live in the same cache
        Properties extraProps = new Properties();
        // put any extra dynamic properties such as show_sql and generate_statistics
        extraProps.putAll(extraProperties);
        // bl: enable scrollable result sets
        extraProps.setProperty(Environment.USE_SCROLLABLE_RESULTSET, "true");
        // bl: with this flag set, Hibernate will attempt to get some settings from the JDBC metadata.
        // this means Hibernate has to acquire a connection at SessionFactory creation time, which we do not support.
        // not necessary anyway since we would rather supply any special settings ourselves, anyway.
        // see org.hibernate.cfg.SettingsFactory:101
        extraProps.setProperty("hibernate.temp.use_jdbc_metadata_defaults", "false");
        //extraProps.setProperty("hibernate.cache.region_prefix", configSet);

        cfg.addProperties(extraProps);

        //Extract and add Redisson Hibernate configuration properties
        //This is really kludgy but the only other option for managing this config is to add all of these properties to
        //hibernate.cfg.xml or override all cache loading behavior in
        //{@link org.redisson.hibernate.RedissonLocalCachedRegionFactory}
        NarrativeProperties narrativeProperties = StaticConfig.getBean(NarrativeProperties.class);
        Properties redissonProps = ManagedRedissonLocalCachedRegionFactory.loadCacheProperties();
        cfg.addProperties(redissonProps);

        //add a load listener to clear out the proxy thread locals that hibernate and cglib are leaking
        // bl: commenting out, as this was fixed in Hibernate 3.2.3:
        // http://opensource.atlassian.com/projects/hibernate/browse/HHH-2481
        /*cfg.setListeners("load", new LoadEventListener [] {new DefaultLoadEventListener(), new LoadEventListener() {
            public void onLoad(LoadEvent event, LoadType loadType) throws HibernateException {
                Object obj = event.getResult();
                if (obj instanceof HibernateProxy) {
                    Enhancer.registerCallbacks(obj.getClass(),null);
                }
            }
        }});*/

        //Late bind our listener registration function due to chicken and egg issues once again
        cfg.getListenerIntegrator().setListenerRegistrationConsumer(registerListeners);

        // bl: use the legacy connection handling mode of delayed acquisition, but close connection on session close.
        // this will enable us to commit transactions while maintaining the same connection. this is needed
        // particularly for temp table management during the rewards process. refer: #3192
        cfg.setProperty(AvailableSettings.CONNECTION_HANDLING, PhysicalConnectionHandlingMode.DELAYED_ACQUISITION_AND_HOLD.name());

        //Register custom types so SchemaExport will use them
        registerCustomTypes(cfg);

        sessionFactory = cfg.buildSessionFactory();

        //Keep a reference to the MetadataExtractor
        metadataExtractorIntegrator = cfg.getMetadataExtractorIntegrator();
    }

    /**
     * Register custom {@link BasicType} types
     */
    private void registerCustomTypes(NetworkAnnotationConfiguration cfg) {
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        // Filter to include only classes that implement BasicType
        provider.addIncludeFilter(new AssignableTypeFilter(BasicType.class));
        // Find classes in the given package (or subpackages)
        Set<BeanDefinition> beans = provider.findCandidateComponents("org.narrative.common.persistence");
        for (BeanDefinition bd : beans) {
            try {
                cfg.registerTypeOverride((BasicType) Class.forName(bd.getBeanClassName()).newInstance());
                logger.debug("Registered custom BasicType " + bd.getBeanClassName() + " with Hibernate");
            } catch (Exception e) {
                throw UnexpectedError.getRuntimeException("Error instantiating class " + bd.getBeanClassName() + " for Hibernate type registration", e);
            }
        }
    }

    /**
     * Function to register listeners with a passed in {@link ServiceRegistry}
     */
    private final Consumer<ServiceRegistry> registerListeners = (serviceRegistry) -> {
        EventDispatcher dispatcher = new EventDispatcher();

        EventListenerRegistry registry = serviceRegistry.getService(EventListenerRegistry.class);

        registry.setListeners(EventType.PRE_INSERT, dispatcher);
        registry.setListeners(EventType.POST_INSERT, dispatcher);
        registry.setListeners(EventType.PRE_UPDATE, dispatcher);
        registry.setListeners(EventType.POST_UPDATE, dispatcher);
        registry.setListeners(EventType.PRE_DELETE, dispatcher);
        registry.setListeners(EventType.POST_DELETE, dispatcher);
        registry.setListeners(EventType.PRE_LOAD, dispatcher);
        registry.setListeners(EventType.POST_LOAD, dispatcher);
    };

    public void setCurrentSession(GSession currentSession) {
        if (currentSession == null) {
            this.currentSession.remove();
        } else {
            this.currentSession.set(currentSession);
        }
    }

    public boolean hasCurrentSession() {
        return currentSession.get() != null;
    }

    @NotNull
    public GSession getCurrentSession() {
        assert currentSession.get() != null : "Current session has not been set for this GSessionFactory,thread.  Partition Type: " + getName();
        return currentSession.get();
    }

    public PartitionType getPartitionType() {
        return partitionType;
    }

    public String getName() {
        return name;
    }

    public <T extends DAOObject, ID extends Serializable> List<ID> getIDsFromObjects(Collection<T> objs) {
        if (objs == null) {
            return Collections.emptyList();
        }
        List<ID> ret = new ArrayList<>(objs.size());
        for (T obj : objs) {
            ID id = getIdentifier(obj);
            if (id != null) {
                ret.add(id);
            }
        }
        return ret;
    }

    public void close() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

    /**
     * This class is the internal event dispatcher for all hibernate events
     */
    public class EventDispatcher implements PreLoadEventListener, PostLoadEventListener, PostInsertEventListener, PostUpdateEventListener, PostDeleteEventListener, EvictEventListener, PreInsertEventListener, PreUpdateEventListener, PreDeleteEventListener {
        @Override
        public void onPreLoad(PreLoadEvent event) {
            for (EventListener listener : getEventListenersForEvent(event.getEntity().getClass())) {
                listener.onPreLoad(event);
            }
        }

        @Override
        public void onPostLoad(PostLoadEvent event) {
            for (EventListener listener : getEventListenersForEvent(event.getEntity().getClass())) {
                listener.onPostLoad(event);
            }
        }

        @Override
        public boolean onPreInsert(PreInsertEvent event) {
            for (EventListener listener : getEventListenersForEvent(event.getEntity().getClass())) {
                listener.onEventListenerPreInsert(event);
            }

            return false;
        }

        @Override
        public void onPostInsert(PostInsertEvent event) {
            for (EventListener listener : getEventListenersForEvent(event.getEntity().getClass())) {
                listener.onPostInsert(event);
            }
        }

        @Override
        public boolean onPreUpdate(PreUpdateEvent event) {
            for (EventListener listener : getEventListenersForEvent(event.getEntity().getClass())) {
                listener.onEventListenerPreUpdate(event);
            }

            return false;
        }

        @Override
        public void onPostUpdate(PostUpdateEvent event) {
            for (EventListener listener : getEventListenersForEvent(event.getEntity().getClass())) {
                listener.onPostUpdate(event);
            }
        }

        @Override
        public boolean onPreDelete(PreDeleteEvent event) {
            for (EventListener listener : getEventListenersForEvent(event.getEntity().getClass())) {
                listener.onEventListenerPreDelete(event);
            }

            return false;
        }

        @Override
        public void onPostDelete(PostDeleteEvent event) {
            for (EventListener listener : getEventListenersForEvent(event.getEntity().getClass())) {
                listener.onPostDelete(event);
            }
        }

        @Override
        public void onEvict(EvictEvent event) throws HibernateException {

            for (EventListener listener : getEventListenersForEvent(event.getObject().getClass())) {
                listener.onEvict(event);
            }

            //don't forget to tell hibernate about it;
            evictEventListener.onEvict(event);
        }

        private List<EventListener> getEventListenersForEvent(Class cls) {
            // return a list so that they are ordered
            List<EventListener> ret = new LinkedList<>();

            // first add all of the global listeners to the list
            ret.addAll(globalListeners.keySet());

            // now add all of the class listeners
            // bl: we need to support class listeners for inheritance hierarchies, so go up
            // the hierarchy until we find the event listeners for this class.
            while (true) {
                if (!DAOObject.class.isAssignableFrom(cls)) {
                    break;
                }
                Set<EventListener> clsList = classListeners.get(cls);
                if (clsList != null) {
                    ret.addAll(clsList);
                }
                cls = cls.getSuperclass();
            }

            // finally, add all session event listeners
            // bl: removed session event listeners for now since they weren't being used at all
            /*for (GSession sess : sessions.values()) {
                if (sess.getSession().isOpen()) {
                    ret.addAll(sess.getSessionEventListeners());
                }
            }*/

            return ret;
        }

        private static final long serialVersionUID = -926783743083221388L;

        /**
         * Does this listener require that after transaction hooks be registered?
         *
         * @param persister The persister for the entity in question.
         * @return {@code true} if after transaction callbacks should be added.
         * @deprecated use {@link #requiresPostCommitHandling(EntityPersister)}
         */
        @Override
        public boolean requiresPostCommitHanding(EntityPersister persister) {
            return true;
        }

        /**
         * Does this listener require that after transaction hooks be registered?
         *
         * @param persister The persister for the entity in question.
         * @return {@code true} if after transaction callbacks should be added.
         */
        @Override
        public boolean requiresPostCommitHandling(EntityPersister persister) {
            return true;
        }
    }

    /**
     * get the identifier for the given object
     *
     * @param o the object to get the identifier for
     * @return a Serializable object representing the identifier for the given object
     */
    @Nullable
    public <T extends Serializable> T getIdentifier(Object o) {
        if (o == null) {
            return null;
        }
        // if the object is a HibernateProxy, then use the LazyInitializer to
        // get the identifier.
        LazyInitializer li = HibernateUtil.getLazyInitializer(o);
        if (li != null) {
            // HibernateProxyHelper _almost_ does what we want, but not quite.
            // it has methods to get the class from a HibernateProxy object.
            // it also has a method (getIdentifier) to get the identifier from an object, but you
            // must provide to the method a ClassPersister object.  ClassPersister
            // objects are only available via the SessionFactoryImplementor
            // and SessionImplementor interfaces.  neither of these are directly
            // exposed to us, so we'd have to check if the Session/SessionFactory
            // are instances of the corresponding interface and cast accordingly.
            return (T) li.getIdentifier();
        }

        Class clazz = o.getClass();
        ClassMetadata cmd = sessionFactory.getClassMetadata(clazz);
        if (cmd == null) {
            throw new HibernateException("Can't get an identifier for an object not defined in Hibernate hbm files! class: " + o.getClass());
        }
        return (T) cmd.getIdentifier(o);
    }

    /**
     * Evicts a single object from the second-tier cache (SwarmCache)
     *
     * @param cls
     * @param oid
     */
    public void evictSecondTier(Class cls, OID oid) {
        sessionFactory.getCache().evict(cls, oid);
        CacheManager.invalidateObject(cls, oid, this.getCurrentSession());
    }

    /**
     * Evicts an entire class of objects from the second-tier cache (SwarmCache).
     * This is currently NOT sensitive to the current DBSet.  It
     * will eveict all obejcts of a specified type from all DBSets
     *
     * @param cls
     */
    public void evictSecondTier(Class cls) {
        sessionFactory.getCache().evict(cls);
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public MetadataExtractorIntegrator getMetadataExtractorIntegrator() {
        return metadataExtractorIntegrator;
    }
}
