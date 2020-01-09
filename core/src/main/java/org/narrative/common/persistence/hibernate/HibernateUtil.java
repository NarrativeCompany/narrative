package org.narrative.common.persistence.hibernate;

import org.narrative.common.util.NarrativeLogger;
import org.narrative.common.util.UnexpectedError;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.LazyInitializationException;
import org.hibernate.UnresolvableObjectException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.jetbrains.annotations.Nullable;

import java.sql.Timestamp;
import java.util.Iterator;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Aug 10, 2004
 * Time: 11:57:45 AM
 * <p>
 * This class holds several utility methods for working with Hibernate.
 */
public class HibernateUtil {

    private static final NarrativeLogger logger = new NarrativeLogger(HibernateUtil.class);

    /**
     * the name for the generic generator.  doesn't really matter much.
     */
    public static final String FOREIGN_GENERIC_GENERATOR_NAME = "foreign_generator";

    /**
     * the Hibernate-specific "foreign" strategy to use for the OID field.
     * see @org.hibernate.id.ForeignGenerator for more information
     */
    public static final String FOREIGN_STRATEGY = "foreign";
    /**
     * the lone "property" parameter to use with Hibernate's "foreign" generator
     */
    public static final String FOREIGN_STRATEGY_PROPERTY_NAME = "property";

    /**
     * if you specify "none" as the name of the foreign key on Hibernate's @ForeignKey annotation,
     * then Hibernate will exclude that foreign key from being created.
     */
    public static final String NO_FOREIGN_KEY_NAME = "none";

    /**
     * bl: pretty lame, but we need this for one-to-one associations from true entities to view entities.
     * used in the case of the GlobalContent->GlobalContentRights association.
     * they both have non-nullable bi-directional one-to-one associations, so we need to skip them for purposes
     * of our validation in NarrativeAnnotationConfiguration.
     */
    public static final String VIEW_FOREIGN_KEY_SPECIAL_NAME = "SPECIAL_VIEW_FK";

    /**
     * Returns a an object cast to the type specified or null if the object is not assignable to that type
     */
    @Nullable
    public static <T, C extends T> C castObject(T obj, Class<C> cls) {
        if (obj == null) {
            return null;
        }
        // bl: don't unwrap the object from the proxy if we don't have to
        if (cls.isAssignableFrom(obj.getClass())) {
            return (C) obj;
        }
        if (isObjectOfType(obj, cls)) {
            return (C) getConcreteClassInstance(obj);
        }
        return null;
    }

    /**
     * get the concrete class instance for the specified object.
     * the specified object can be a HibernateProxy in which case
     * we'll inspect the LazyInitializer to determine what the actual
     * implementation of the proxy is.  if the object isn't a HibernateProxy,
     * then we'll just return the object.
     *
     * @param o the object to get the concrete class instance for
     * @return the concrete class instance for the specified object.
     * the object may be a HibernateProxy in which case inspection
     * will take place to determine the object's concrete instance.
     */
    public static <T> T getConcreteClassInstance(T o) {
        LazyInitializer li = getLazyInitializer(o);
        if (li == null) {
            return o;
        }

        T impl = (T) li.getImplementation();
        if (impl == null) {
            throw UnexpectedError.getRuntimeException("Failed getting the implementation of the class from the LazyInitializer for the specified HibernateProxy! o/" + o, new Throwable(), true);
        }
        return impl;
    }

    public static Class getConcreteClass(Object o) {
        if (o == null) {
            return null;
        }

        LazyInitializer li = getLazyInitializer(o);
        if (li == null) {
            return o.getClass();
        }

        return li.getPersistentClass();
    }

    /**
     * determine if a given object is initialized.
     * returns true if the object is not a proxy.
     * returns true if the object is a proxy that has been initialized.
     * returns false if the object is a proxy that has not yet been initialized.
     *
     * @param o the object to test
     * @return true if the object is initialized.  false if it is not.
     */
    public static boolean isObjectInitialized(Object o) {
        LazyInitializer li = getLazyInitializer(o);
        if (li == null) {
            return true;
        }

        return !li.isUninitialized();
    }

    /**
     * initialize a Hibernate object (if it is a Proxy)
     *
     * @param o the object to initialize
     * @throws LazyInitializationException if the object doesn't exist in the database.
     *                                     actually, i can't confirm that this actually happens.  from my experience,
     *                                     an ObjectNotFoundException is thrown.  leaving here for completeness, however.
     * @throws UnresolvableObjectException if the object isn't found. in my experience, the instance
     *                                     is actually an ObjectNotFoundException, but i've chosen to throw the UnresolvableObjectException
     *                                     (the parent class of ObjectNotFoundException) instead.
     */
    public static void initializeObject(Object o) throws LazyInitializationException, UnresolvableObjectException {
        LazyInitializer li = getLazyInitializer(o);
        if (li == null) {
            return;
        }
        li.initialize();
    }

    /**
     * attempt to initialize the given object.  if the object doesn't
     * exist, then false will be returned.
     *
     * @param o the object to test for existence.
     * @return true if the object exists in the database.  false if it does not.
     * returns false if the object is null, as well.
     */
    public static boolean doesObjectExist(Object o) {
        if (o == null) {
            return false;
        }
        try {
            LazyInitializer li = getLazyInitializer(o);
            if (li == null) {
                return true;
            }
            li.initialize();
            return li.getImplementation() != null;
        } catch (LazyInitializationException lie) {
            return false;
        } catch (UnresolvableObjectException onfe) {
            return false;
        }
    }

    /**
     * get the LazyInitializer for the given object
     *
     * @param o the object to get the LazyInitializer for
     * @return the LazyInitializer for this object or null if this
     * object is not a HibernateProxy.
     */
    @Nullable
    public static LazyInitializer getLazyInitializer(Object o) {
        if (o == null) {
            return null;
        }
        if (!(o instanceof HibernateProxy)) {
            return null;
        }
        // bl: oh so similar to HibernateProxyHelper.getClass(),
        // but it doesn't quite do what we are looking for.

        LazyInitializer li = ((HibernateProxy) o).getHibernateLazyInitializer();
        if (li == null) {
            throw UnexpectedError.getRuntimeException("Failed identifying the LazyInitializer for the specified HibernateProxy! o/" + o, new Throwable(), true);
        }
        return li;
    }

    /**
     * determine if the given Hibernate object is of the
     * specified Class type
     *
     * @param o     the object to inspect
     * @param clazz the class to check if the object is an instance (assignable from)
     * @return true if the specified object is assignable from the specified class.
     */
    public static <T> boolean isObjectOfType(T o, Class<? extends T> clazz) {
        if (o == null) {
            return false;
        }
        Class objClass = o.getClass();
        if (clazz.isAssignableFrom(objClass)) {
            return true;
        }
        T implementation = getConcreteClassInstance(o);
        if (implementation == null) {
            return false;
        }
        Class implClass = implementation.getClass();
        if (implClass == null) {
            return false;
        }
        return clazz.isAssignableFrom(implClass);
    }

    /**
     * Close an Iterator created by iterate() immediately,  instead of waiting until the session is closed or disconnected.
     *
     * @param i the iterator to close
     */
    public static void closeIterator(Iterator i) {
        Hibernate.close(i);
    }

    private static String buildName(String alias, String... propertyNames) {
        StringBuilder sb = new StringBuilder(alias);
        for (String propertyName : propertyNames) {
            sb.append('.');
            sb.append(propertyName);
        }
        return sb.toString();
    }

    public static String makeName(Criteria criteria, String... propertyNames) {
        return buildName(criteria.getAlias(), propertyNames);
    }

    public static Order getOrder(String propertyName, boolean ascending) {
        if (ascending) {
            return Order.asc(propertyName);
        } else {
            return Order.desc(propertyName);
        }
    }

    public static Criterion getEqualCriterion(String property, Object value, boolean isNot) {
        if (!isNot) {
            return Restrictions.eq(property, value);
        } else {
            return Restrictions.ne(property, value);
        }
    }

    public static void addLongRangeCriteria(Criteria criteria, String propertyName, Long min, Long max) {
        if (min == null && max == null) {
            return;
        }

        String fieldName = makeName(criteria, propertyName);
        if (isEqual(min, max)) {
            criteria.add(Restrictions.eq(fieldName, min.intValue()));

        } else {
            if (min != null) {
                criteria.add(Restrictions.ge(fieldName, min.intValue()));
            }
            if (max != null) {
                criteria.add(Restrictions.le(fieldName, max.intValue()));
            }
        }
    }

    public static void addDatetimeRangeCriteria(Criteria criteria, String propertyName, Timestamp after, Timestamp before) {
        if (after == null && before == null) {
            return;
        }

        String fieldName = HibernateUtil.makeName(criteria, propertyName);
        // jw: this will only ever be true for BetweenTimestampsHelper results!
        if (isEqual(after, before)) {
            criteria.add(Restrictions.eq(fieldName, before));

        } else {
            if (after != null) {
                criteria.add(Restrictions.ge(fieldName, after));
            }

            if (before != null) {
                criteria.add(Restrictions.le(fieldName, before));
            }
        }
    }
}


