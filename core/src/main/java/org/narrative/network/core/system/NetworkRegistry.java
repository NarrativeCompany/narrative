package org.narrative.network.core.system;

import org.narrative.common.cache.CacheManager;
import org.narrative.common.persistence.DAOImpl;
import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.GSessionContainer;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.OIDGenerator;
import org.narrative.common.persistence.ObjectPair;
import org.narrative.common.persistence.ObjectTriplet;
import org.narrative.common.persistence.PartitionGroupInfo;
import org.narrative.common.persistence.PartitionGroupOptions;
import org.narrative.common.persistence.PersistenceUtil;
import org.narrative.common.persistence.hibernate.DatabaseResources;
import org.narrative.common.persistence.hibernate.GSession;
import org.narrative.common.persistence.hibernate.GSessionFactory;
import org.narrative.common.util.Configuration;
import org.narrative.common.util.Debug;
import org.narrative.common.util.IPStringUtil;
import org.narrative.common.util.IPUtil;
import org.narrative.common.util.MailUtil;
import org.narrative.common.util.RuntimeUtils;
import org.narrative.common.util.UnexpectedError;
import org.narrative.config.StaticConfig;
import org.narrative.config.properties.NarrativeProperties;
import org.narrative.network.core.area.base.services.ItemHourTrendingStatsManager;
import org.narrative.network.core.area.user.dao.AreaUserDAO;
import org.narrative.network.core.cluster.partition.Partition;
import org.narrative.network.core.cluster.partition.PartitionBalancer;
import org.narrative.network.core.cluster.partition.PartitionConnectionPool;
import org.narrative.network.core.cluster.partition.PartitionGroup;
import org.narrative.network.core.cluster.partition.PartitionType;
import org.narrative.network.core.composition.base.Reply;
import org.narrative.network.core.content.base.Content;
import org.narrative.network.core.content.base.FileDataWrapper;
import org.narrative.network.core.fileondisk.base.services.FileUploadUtils;
import org.narrative.network.core.fileondisk.image.ImageOnDisk;
import org.narrative.network.core.fileondisk.image.ImageOnDiskEventListener;
import org.narrative.network.core.propertyset.area.AreaPropertyOverride;
import org.narrative.network.core.propertyset.area.AreaPropertyOverrideEventListener;
import org.narrative.network.core.propertyset.area.AreaPropertySet;
import org.narrative.network.core.propertyset.area.AreaPropertySetEventListener;
import org.narrative.network.core.propertyset.base.Property;
import org.narrative.network.core.propertyset.base.PropertyEventListener;
import org.narrative.network.core.propertyset.base.PropertySet;
import org.narrative.network.core.propertyset.base.PropertySetEventListener;
import org.narrative.network.core.propertyset.base.dao.PropertySetDAO;
import org.narrative.network.core.propertyset.base.services.InitializePropertySetType;
import org.narrative.network.core.propertyset.base.services.PropertySetTypeBase;
import org.narrative.network.core.quartz.services.QuartzJobScheduler;
import org.narrative.network.core.search.IndexHandlerManager;
import org.narrative.network.core.search.services.SearchItemEventListener;
import org.narrative.network.core.settings.area.community.advanced.SandboxedCommunitySettings;
import org.narrative.network.core.settings.global.DefaultGlobalSettings;
import org.narrative.network.core.settings.global.GlobalSettings;
import org.narrative.network.core.settings.global.services.GlobalSettingsUtil;
import org.narrative.network.core.settings.global.services.translations.TranslationRegistry;
import org.narrative.network.core.user.AuthRealm;
import org.narrative.network.core.user.ClusterCpAuthRealm;
import org.narrative.network.core.user.NarrativeAuthZoneMaster;
import org.narrative.network.core.user.User;
import org.narrative.network.core.versioning.AppliedPatch;
import org.narrative.network.core.versioning.dao.AppVersionDAO;
import org.narrative.network.core.versioning.services.PatchRegistry;
import org.narrative.network.core.versioning.services.PatchRunner;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.customizations.narrative.publications.Publication;
import org.narrative.network.customizations.narrative.publications.PublicationSettings;
import org.narrative.network.customizations.narrative.services.GoogleAnalyticsUtil;
import org.narrative.network.shared.context.RequestType;
import org.narrative.network.shared.daobase.NetworkDAOImpl;
import org.narrative.network.shared.email.NetworkMailUtil;
import org.narrative.network.shared.services.NetworkAnnotationConfiguration;
import org.narrative.network.shared.services.ServerRegistry;
import org.narrative.network.shared.servlet.StaticFilterUtils;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;
import org.narrative.network.shared.tasktypes.TaskRunner;
import org.narrative.network.shared.util.NetworkLogger;
import com.opensymphony.xwork2.ognl.OgnlNullHandlerWrapper;
import net.fortuna.ical4j.util.CompatibilityHints;
import ognl.OgnlRuntime;
import org.apache.commons.lang.StringUtils;
import org.hibernate.boot.model.naming.ImplicitNamingStrategyComponentPathImpl;
import org.opensaml.DefaultBootstrap;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Nov 29, 2005
 * Time: 5:37:15 PM
 */
public class NetworkRegistry {

    private static final NetworkLogger logger = new NetworkLogger(NetworkRegistry.class);

    public static final String NETWORK_REGISTRY = "networkRegistry";

    private static final String CLUSTER_CP_DOMAIN = "clusterCpDomain";
    private static final String CLUSTER_ID = "clusterId";
    private static final String HEARTBEAT_SERVER_PORT = "heartbeatServerPort";
    private static final String DIRECT_SERVLET_HEARTBEAT_SERVER_PORT = "directServletHeartbeatServerPort";

    private static final String SMTP_SERVER = "smtpServer";
    private static final String SMTP_PORT = "smtpPort";
    private static final String SMTP_USE_TLS = "smtpUseTls";
    private static final String SMTP_USERNAME = "smtpUsername";
    private static final String SMTP_PASSWORD = "smtpPassword";
    private static final String DISABLE_EMAIL_DELIVERY = "disableEmailDelivery";
    private static final String TEST_EMAIL_TO_USE = "testEmailToUse";

    private static final String JDBC_DRIVER_CLASS = "jdbc.driver";

    private static final NetworkRegistry instance = new NetworkRegistry();

    private static final String MYSQL_BINARY_PATH = "mysqlBinaryPath";

    private static final String RECAPTCHA_PUBLIC_KEY = "recaptchaPublicKey";
    private static final String RECAPTCHA_PRIVATE_KEY = "recaptchaPrivateKey";

    private static final String GOOGLE_ANALYTICS_API_JSON_FILE = "googleAnalyticsApiJsonFile";
    private static final String GOOGLE_ANALYTICS_API_VIEW_ID = "googleAnalyticsApiViewId";

    public static final String INTERNAL_JSP_RUNNER_CONFIG_PARAM_PREFIX = "server.servlet.jsp.init-parameters.";

    private static final String SOLR_SERVER_URL = "solrServerUrl";
    private static final String SOLR_ZOOKEEPER_HOST = "solrZookeeperHost";
    private static final String SOLR_CLOUD_DEFAULT_COLLECTION = "solrCloudDefaultCollection";

    public static final String ENABLE_ENCRYPTION = "enableEncryption";

    // bl: keep these settings in sync with the jsp servlet init-params in web.xml.
    private static final Collection<ObjectPair<String, String>> DEFAULT_JSP_RUNNER_SETTINGS =
            Arrays.asList(new ObjectPair<>("fork", "false"),
                    new ObjectPair<>("xpoweredBy", "false"),
                    new ObjectPair<>("trimSpaces", "true"),
                    new ObjectPair<>("enablePooling", "false"),
                    // bl: removing this. don't really want modern now since we aren't including ant in the path. see JspCompilationContext.createCompiler() order of ops.
                    // "modern" just means try ant first, which seems to always fail. refer: OPS-1535
                    //new ObjectPair<>("compiler", "modern"),
                    new ObjectPair<>("compilerTargetVM", "1.8"),
                    new ObjectPair<>("compilerSourceVM", "1.8"));

    private final List<ObjectPair<String, Runnable>> END_OF_INIT_RUNNABLES = new LinkedList<>();

    static Properties loadProperties(String filename) {
        Properties p = new Properties();
        try {
            p.load(NetworkRegistry.class.getResourceAsStream(filename));
        } catch (IOException ioex) {
            logger.error("Could not properties " + filename + ", server broken.", ioex);
            throw UnexpectedError.getRuntimeException("Could not properties " + filename, ioex);
        }
        return p;
    }

    public static NetworkRegistry getInstance() {
        return instance;
    }

    public static final OID GLOBAL_PARTITION_OID = new OID(123123123123L);  //todo: make this a real UUID or whatever - is there a risk of collision with these hard coded OIDs?

    private String servletName;
    private boolean isWebapp;
    private boolean isInstalling;
    private ThreadLocal<Boolean> isImporting = new ThreadLocal<>();
    private EnvironmentType environmentType = EnvironmentType.UNINITIALIZED;

    // bl: going to use the base part of the domain as the clusterId for use in reference IDs for errors.
    private String clusterId;

    private String narrativePlatformDomain;
    private String narrativePlatformUrl;
    private String clusterCpDomain;
    private Integer clusterCpPort;
    private String clusterCpUrl;

    private String versionStringForPatches;
    private String versionStringForPath;
    private String staticPath;

    private int heartbeatServerPort;
    private int directServletHeartbeatServerPort;

    private String smtpServer;
    private int smtpPort;
    private boolean smtpUseTls;
    private String smtpUsername;
    private String smtpPassword;
    private boolean disableEmailDelivery;
    private String testEmailToUse;

    private String mysqlBinaryPath;

    private long serverStartTime;
    private long servletContextLastModifiedTime;

    private String reCaptchaPublicKey;
    private String reCaptchaPrivateKey;

    private String solrServerUrl;
    private List<String> solrZookeeperHosts;
    private String solrCloudDefaultCollection;

    public String getServletName() {
        return servletName;
    }

    public boolean isWebapp() {
        return isWebapp;
    }

    public boolean isImporting() {
        return isImporting.get() != null && isImporting.get();
    }

    public void setIsImporting(boolean isImporting) {
        if (isImporting) {
            this.isImporting.set(Boolean.TRUE);
        } else {
            this.isImporting.remove();
        }
    }

    public boolean isLocalServer() {
        return environmentType.isLocal();
    }

    public boolean isDevServer() {
        return environmentType.isDev();
    }

    public boolean isStagingServer() {
        return environmentType.isStaging();
    }

    public boolean isLocalOrDevServer() {
        return isLocalServer() || isDevServer();
    }

    public boolean isProductionServer() {
        return environmentType.isProduction();
    }

    public String getVersion() {
        return NetworkVersion.INSTANCE.getVersion();
    }

    public String getClusterId() {
        return clusterId;
    }

    public boolean isNarrativePlatformSsl() {
        return environmentType.isUsesSsl();
    }

    public String getNarrativePlatformDomain() {
        return narrativePlatformDomain;
    }

    public String getClusterCpDomain() {
        return clusterCpDomain;
    }

    public Integer getClusterCpPort() {
        return clusterCpPort;
    }

    public String getClusterCpUrl() {
        return clusterCpUrl;
    }

    public String getClusterCpRelativePath() {
        // bl: for now, no relative path; just empty string
        return "";
    }

    private String narrativeAboutWebsiteUrl;

    public String getNarrativeAboutWebsiteUrl() {
        if (narrativeAboutWebsiteUrl == null) {
            if(isLocalOrDevServer()) {
                narrativeAboutWebsiteUrl = "https://dev-about.narrative.org";
            } else if(isStagingServer()) {
                narrativeAboutWebsiteUrl = "https://staging-about.narrative.org";
            } else {
                narrativeAboutWebsiteUrl = "https://about.narrative.org";
            }
        }
        return narrativeAboutWebsiteUrl;
    }

    private String narrativeKycQueueUrl;

    public String getNarrativeKycQueueUrl() {
        if (narrativeKycQueueUrl == null) {
            if(isLocalOrDevServer()) {
                narrativeKycQueueUrl = "https://dev-kyc-queue.narrative.cloud";
            } else if(isStagingServer()) {
                narrativeKycQueueUrl = "https://staging-kyc-queue.narrative.cloud";
            } else {
                narrativeKycQueueUrl = "https://kyc-queue.narrative.cloud";
            }
        }
        return narrativeKycQueueUrl;
    }

    public String getAcceptableUsePolicyUrl() {
        return getNarrativeAboutWebsiteUrl() + "/assets/documents/narrative-acceptable-use-policy.pdf";
    }

    public String getVersionStringForPath() {
        return versionStringForPath;
    }

    public String getVersionStringForPatches() {
        return versionStringForPatches;
    }

    public String getStaticPath() {
        return staticPath;
    }

    public boolean isDisableEmailDelivery() {
        return disableEmailDelivery;
    }

    public File getTempDir() {
        return new File(System.getProperty("java.io.tmpdir"));
    }

    private boolean initDone = false;

    public boolean isInitDone() {
        return initDone;
    }

    public synchronized void init(boolean isCommandLineApp, boolean isInstall) {
        // bl: LAME!  need to use Sun's internal SAXParserFactory for Struts.  there is a funky XML parsing
        // issue caused by Xerces that leads to a single text node being split up into multiple text nodes
        // which ultimately prevents the servlet from starting up due to Struts action mappings not having
        // a valid result.  to see the problematic code, refer to:
        // NetworkActionMapper.init() around line 352 where we check for the "location" parameter on all actions.
        // XmlConfigurationProvider.buildResults() on line 439. it checks if there is exactly one child node
        // and that child node must be a text node.  well, it turns out that Xerces sometimes splits the text
        // children into multiple nodes (for no apparent reason), which prevents the location from getting mapped
        // correctly.
        //System.setProperty("xwork.saxParserFactory", "com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl");
        // bl: we found huge memory leaks in the Jasper Runtime caused by the char[] cb buffer in BodyContentImpl
        // never getting reset. in order to avoid the memory overhead, we need to set LIMIT_BUFFER to true
        // so that the char[] will get recreated on each JSP creation (following a clear()). we don't really have a choice
        // in doing this since some of our JSPs can be quite large. hopefully the performance hit from recreating these
        // arrays is worth the freeing up of memory that will ensue.
        System.setProperty("org.apache.jasper.runtime.BodyContentImpl.LIMIT_BUFFER", "true");
        // bl: relax the iCal4J parsing so that things like FILENAME (for attachments to Google events) are ignored.
        CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_PARSING, true);

        // bl: bootstrap the OpenSAML configuration so it's loaded and ready to go.
        try {
            DefaultBootstrap.bootstrap();
        } catch (org.opensaml.xml.ConfigurationException e) {
            throw UnexpectedError.getRuntimeException("Failed bootstrapping OpenSAML default configuration!", e);
        }

        this.isInstalling = isInstall;

        serverStartTime = System.currentTimeMillis();
        Configuration cfg = Configuration.getConfiguration();

        registerGlobalEnvVariables(cfg);

        //load the email mappings
        EmailToJSPMapping.INSTANCE.init();

        MailUtil.init(smtpServer, smtpPort, smtpUsername, smtpPassword, smtpUseTls);

        // bl: added these two initialization workarounds to resolve common code dependencies on network code.
        NetworkLogger.init();

        servletContextLastModifiedTime = Long.valueOf(cfg.getProperty(Configuration.SERVLET_CONTEXT_LAST_MODIFIED, Long.toString(serverStartTime)));

        QuartzJobScheduler.LOCAL.start();

        // now, as long as we aren't a command line app, we should always set ourself up to be a webapp
        isWebapp = !isCommandLineApp;

        assert !PartitionType.GLOBAL.isInit() : "Should never call NetworkRegistry.init twice!";

        //init cache manager
        CacheManager.init();

        PartitionGroupInfo.init(new PartitionGroupOptions() {
            @Override
            public void addEndOfPartitionGroupRunnableForSuccessOrError(Runnable runnable) {
                PartitionGroup.addEndOfPartitionGroupRunnableForSuccessOrError(runnable);
            }
        });

        CacheManager.setSessions(new GSessionContainer() {
            public Collection<GSession> getSessions() {
                return PartitionGroup.getCurrentPartitionGroup().getOpenSessions().values();
            }
        });

        // bl: no longer going to enable hibernate statistics by default on dev and QA servers. must be enabled
        // manually in context.xml.
        boolean enableHibernateStatistics = Boolean.valueOf(cfg.getProperty("enableHibernateStatistics", "false"));
        GSessionFactory.setEnableHibernateStatistics(enableHibernateStatistics);

        {
            Map<PartitionType, Partition> singletonPartitionTypeToPartition = new HashMap<PartitionType, Partition>();
            //set up the global partition
            Partition globalPartition = new Partition(PartitionType.GLOBAL);
            setupPartition(globalPartition, cfg, GLOBAL_PARTITION_OID);
            singletonPartitionTypeToPartition.put(PartitionType.GLOBAL, globalPartition);

            for (PartitionType type : PartitionType.ACTIVE_PARTITION_TYPES) {
                // for now we'll always ask for the patching queries, since any servlet can patch
                // todo: optimize to remove the queries when done patching
                type.init(true, isInstalling, singletonPartitionTypeToPartition.get(type));
            }
        }

        //configure the partition session factories
        for (PartitionType type : PartitionType.ACTIVE_PARTITION_TYPES) {
            registerDAOImplClasses(type);
            type.setConfiguration(createPartitionConfig(type));
            String dialect = type.getConfiguration().getProperty("hibernate.dialect");
            type.setDatabaseType(dialect);
        }

        //init the OIDGenerator
        // bl: changed to initialize after configuring the partitions so that we can do a root network task
        initOIDGen();

        PatchRegistry.init();

        // bl: never run the patches automatically if this is a command line app.
        Boolean isAppUpToDate = null;
        if (!isCommandLineApp) {
            isAppUpToDate = AppVersionDAO.isUpToDateForBootstrap(NetworkRegistry.getInstance().getVersionStringForPatches());
            // bl: null indicates we couldn't determine the app was up-to-date, which indicates bootstrap patches
            // probably need to be run.  otherwise, if we could determine definitively if the app was up to date
            // (possible if the AppVersion table is in good working order), then we should only run the bootstrap
            // patches if we are not yet up to date.
            if (isAppUpToDate == null || !isAppUpToDate) {
                PatchRunner.runBootstrapPatches();
            }
        }

        // bl: during install, we have to register the partitions as we create them.
        if (!isInstalling) {
            PartitionConnectionPool.initGlobalPartitionConnectionPool();

            // bl: singleton partitions are controlled via context.xml.  when partition info changes, we need
            // to update the corresponding info in the Partition table in the database for consistency.
            Partition.updateSingletonPartitionsInDb();

            PartitionConnectionPool.initPartitionConnectionPools();
        }

        //moved network file hander init before patches, so patches can use files
        //pm: chat server does need this
        // bl: need the NetworkFileHandler in some patches, so test that, too.

        //register the JDBC Driver
        try {
            Class.forName(cfg.getProperty(JDBC_DRIVER_CLASS, "com.mysql.jdbc.Driver"));
        } catch (ClassNotFoundException e) {
            throw UnexpectedError.getRuntimeException("Unable to load specified JDBC Driver", e);
        }

        // initialize all of the PropertySetTypes
        for (Class<? extends PropertySetTypeBase> propertySetTypeDef : NetworkRegistry.PROPERTY_SET_TYPE_DEFS) {
            new InitializePropertySetType(propertySetTypeDef).doTask();
        }

        // register the event listener for the content indexing
        SearchItemEventListener searchItemEventListener = new SearchItemEventListener();
        PartitionType.GLOBAL.getGSessionFactory().registerClassEventListener(searchItemEventListener, User.class, Content.class, Niche.class, Publication.class);
        PartitionType.COMPOSITION.getGSessionFactory().registerClassEventListener(searchItemEventListener, Reply.class);

        // register the event listeners for PropertySets and Properties
        PartitionType.GLOBAL.getGSessionFactory().registerClassEventListener(new PropertySetEventListener(), PropertySet.class);
        PartitionType.GLOBAL.getGSessionFactory().registerClassEventListener(new PropertyEventListener(), Property.class);
        PartitionType.GLOBAL.getGSessionFactory().registerClassEventListener(new ImageOnDiskEventListener(), ImageOnDisk.class);

        // register a listener for partition events
        PartitionType.GLOBAL.getGSessionFactory().registerClassEventListener(new PartitionBalancer.PartitionListener(), Partition.class);

        // register the event listeners for AreaPropertySets and AreaPropertyOverrides
        PartitionType.GLOBAL.getGSessionFactory().registerClassEventListener(new AreaPropertySetEventListener(), AreaPropertySet.class);
        PartitionType.GLOBAL.getGSessionFactory().registerClassEventListener(new AreaPropertyOverrideEventListener(), AreaPropertyOverride.class);

        AreaUserDAO.init();
        PropertySetDAO.init();

        // bl: never run the patches automatically if this is a command line app.
        if (!isCommandLineApp) {
            //now see if we need to run the regular patches
            if (isAppUpToDate == null) {
                // if we couldn't previously determine if we were up to date, then there must have been
                // bootstrap patches required in order to query the AppliedPatch table.  thus, in this case,
                // we need to check again if we are up to date, and this time, it should always return
                // a non-null value.
                isAppUpToDate = AppVersionDAO.isUpToDate(NetworkRegistry.getInstance().getVersionStringForPatches());
                assert isAppUpToDate != null : "Should ALWAYS get a value for isAppUpToDate after bootstrap patches have run!";
            }
            if (!isAppUpToDate) {
                // double check the app version
                if (!AppVersionDAO.isUpToDate(NetworkRegistry.getInstance().getVersionStringForPatches())) {
                    //now run the patches
                    if (!TaskRunner.doRootGlobalTask(new PatchRunner(false, false))) {
                        throw UnexpectedError.getRuntimeException("PatchRunner was not able to run the patches, do not start server unless resolved", true);
                    }
                }
            }
        }

        if (!isInstalling) {
            // bl: initialize the default PropertySets so that they are cached for the life of the servlet
            TaskRunner.doRootGlobalTask(new GlobalTaskImpl<Object>() {
                @Override
                protected Object doMonitoredTask() {
                    PropertySet.dao().initializeDefaultPropertSets();
                    return null;
                }
            });
        }

        // bl: now we can initialize the display resources. note that these may have already been initialized
        // if patches ran above. but if patches didn't run, then we need to initialize them here.
        TaskRunner.doRootGlobalTask(new InitDisplayResources());

        if (isWebapp) {
            QuartzJobScheduler.GLOBAL.start();
            //set up the hour statistic manager
            ItemHourTrendingStatsManager.init();
            FileUploadUtils.init();
            IndexHandlerManager.init(solrServerUrl, solrZookeeperHosts, solrCloudDefaultCollection);
        }

        //General Initializations
        // bl: when installing, don't worry about caching the wordlets by default (which won't work, anyway).
        if (!isInstall) {
            TranslationRegistry.init();
        }

        // bl: don't want Struts to try to create FileData objects, so give an ObjectNullHandler
        // to prevent their auto-creation.
        OgnlRuntime.setNullHandler(FileDataWrapper.class, new OgnlNullHandlerWrapper(new FileDataWrapper.FileDataWrapperNullHandler()));

        //handle any end of init runnables
        for (ObjectPair<String, Runnable> pair : END_OF_INIT_RUNNABLES) {
            String name = pair.getOne();
            Runnable runnable = pair.getTwo();
            logger.info("Running End of Init Runnable: " + name + " (" + runnable.getClass() + ")");
            runnable.run();
            logger.info("End of Init Runnable Complete: " + name + " (" + runnable.getClass() + ")");
        }

        //ok, we are set-up so start the heartbeat
        if (isWebapp) {
            ServerRegistry.INSTANCE.addServer(servletName, ServerRegistry.STATIC_UNIQUE_ID);
        }

        initDone = true;
    }

    public void addEndOfInitRunnable(String name, Runnable runnable) {
        assert !initDone : "Runnables can only be added before init has completed";
        END_OF_INIT_RUNNABLES.add(new ObjectPair<>(name, runnable));
    }

    private void setupPartition(Partition partition, Configuration cfg, OID partitionOid) {
        String name = partition.getPartitionType().toString().toLowerCase();
        partition.setServer(cfg.getProperty(name + ".server", "localhost"));
        partition.setDatabaseName(cfg.getProperty(name + ".database", name));
        partition.setUsername(cfg.getProperty(name + ".username", partition.getDatabaseName() + "_user"));
        partition.setPassword(cfg.getProperty(name + ".password", ""));
        partition.setParameters(cfg.getProperty(name + ".parameters", ""));
        partition.setOid(partitionOid);
    }

    private static void chmod(String file, String arg) {
        List<String> command = new LinkedList<String>();
        command.add("chmod");
        command.add(arg);
        command.add(file);

        ObjectTriplet<Boolean, String, String> out = RuntimeUtils.exec(command);
        if (!out.getOne()) {
            throw UnexpectedError.getRuntimeException("Unable to chmod " + file + ".  " + out.getThree());
        }
    }

    private void initOIDGen() {
        if (isInstalling) {
            OIDGenerator.init(1);
            return;
        }

        Connection con;
        try {
            DatabaseResources dr = PartitionType.GLOBAL.getSingletonPartition().getBootstrapDatabaseResources();
            con = dr.getConnection();
            CallableStatement cs = con.prepareCall("{call getNextHighbit(?)}");
            cs.registerOutParameter(1, Types.SMALLINT);
            cs.execute();
            long highBits = cs.getLong(1);
            cs.close();
            OIDGenerator.init(highBits);
        } catch (SQLException e) {
            throw UnexpectedError.getRuntimeException("Unable to get connection for globalPartition", e);
        } finally {
            // bl: call onEndOfThread to cleanup the bootstrap DatabaseResources
            IPUtil.onEndOfThread();
        }
    }

    private void registerGlobalEnvVariables(Configuration cfg) {
        NarrativeProperties narrativeProperties = getNarrativeProperties();

        servletName = narrativeProperties.getCluster().getServletName();

        if(isEmpty(servletName)) {
            ObjectTriplet<Boolean, String, String> out = RuntimeUtils.exec("hostname");
            if(out.getOne()) {
                servletName = IPStringUtil.getTrimmedString(out.getTwo());
            }
            if(isEmpty(servletName)) {
                throw UnexpectedError.getRuntimeException("Failed to identify hostname for servletName! stderr/" + out.getThree());
            }
        }

        environmentType = narrativeProperties.getCluster().getEnvironmentType();
        if(environmentType.isUninitialized()) {
            throw UnexpectedError.getRuntimeException("Should never use " + EnvironmentType.UNINITIALIZED + "!");
        }

        narrativePlatformDomain = narrativeProperties.getCluster().getPlatformDomain();
        narrativePlatformUrl = (isNarrativePlatformSsl() ? "https://" : "http://") + narrativePlatformDomain;

        clusterCpDomain = cfg.getProperty(CLUSTER_CP_DOMAIN).toLowerCase();
        clusterCpPort = narrativeProperties.getCluster().getPort();

        clusterId = cfg.getProperty(CLUSTER_ID);

        heartbeatServerPort = Integer.parseInt(cfg.getProperty(HEARTBEAT_SERVER_PORT, "9090"));
        directServletHeartbeatServerPort = Integer.parseInt(cfg.getProperty(DIRECT_SERVLET_HEARTBEAT_SERVER_PORT, "9091"));

        reCaptchaPublicKey = cfg.getProperty(RECAPTCHA_PUBLIC_KEY);
        reCaptchaPrivateKey = cfg.getProperty(RECAPTCHA_PRIVATE_KEY);

        // bl: Google Analytics file is required on all environments other than local environments
        {
            String googleAnalyticsApiJsonFile = isLocalServer() ? cfg.getProperty(GOOGLE_ANALYTICS_API_JSON_FILE, null) : cfg.getProperty(GOOGLE_ANALYTICS_API_JSON_FILE);
            if(!isEmpty(googleAnalyticsApiJsonFile)) {
                String googleAnalyticsApiViewId = cfg.getProperty(GOOGLE_ANALYTICS_API_VIEW_ID);
                try {
                    GoogleAnalyticsUtil.initGoogleAnalyticsApi(googleAnalyticsApiJsonFile, googleAnalyticsApiViewId);
                } catch (IOException e) {
                    throw UnexpectedError.getRuntimeException("Failed to initialize the Google Analytics API!", e);
                }
            }
        }

        clusterCpUrl = (ClusterCpAuthRealm.INSTANCE.isSslEnabled() ? "https://" : "http://") + clusterCpDomain;
        clusterCpUrl = clusterCpUrl.toLowerCase();

        RequestType.init();

        versionStringForPath = "ver" + getVersion();
        versionStringForPatches = getVersion();
        if (!StaticFilterUtils.isValidVersionPath("/" + versionStringForPath + "/")) {
            throw UnexpectedError.getRuntimeException("Invalid version string for path!  Version string must be of format 0.0.0.0! versionStringForPath: " + versionStringForPath);
        }
        staticPath = "/static-legacy/" + versionStringForPath;

        smtpServer = cfg.getProperty(SMTP_SERVER);
        smtpPort = Integer.parseInt(cfg.getProperty(SMTP_PORT, "587"));
        smtpUseTls = Boolean.valueOf(cfg.getProperty(SMTP_USE_TLS, "true"));
        smtpUsername = cfg.getProperty(SMTP_USERNAME, null);
        // password is required if username is supplied
        if(!isEmpty(smtpUsername)) {
            smtpPassword = cfg.getProperty(SMTP_PASSWORD);
        }
        boolean disableEmailDelivery = Boolean.valueOf(cfg.getProperty(DISABLE_EMAIL_DELIVERY, "false"));
        if(disableEmailDelivery) {
            // bl: only allow disabling of email delivery on non-production environments
            if(isProductionServer()) {
                throw UnexpectedError.getRuntimeException("Should never run with disableEmailDelivery enabled on a production environment!");
            }
            this.disableEmailDelivery = true;
        }
        testEmailToUse = cfg.getProperty(TEST_EMAIL_TO_USE, null);
        if(isProductionServer() && !isEmpty(testEmailToUse)) {
            throw UnexpectedError.getRuntimeException("Should never set testEmailToUse on production environments, as it would block all email delivery!");
        }

        mysqlBinaryPath = cfg.getProperty(MYSQL_BINARY_PATH, "mysql");

        solrServerUrl = IPStringUtil.getStringAfterStripFromEnd(cfg.getProperty(SOLR_SERVER_URL, null), "/");

        // jw: if we do not have a solrServerUrl, then this must be a cloud environment
        if (StringUtils.isEmpty(solrServerUrl)) {
            solrZookeeperHosts = new LinkedList<>();

            // jw: this first read is required
            String solrZookeeperHost = IPStringUtil.getStringAfterStripFromEnd(cfg.getProperty(SOLR_ZOOKEEPER_HOST), "/");
            while (!StringUtils.isEmpty(solrZookeeperHost)) {
                solrZookeeperHosts.add(solrZookeeperHost);

                // jw: this subsequent read is optional.
                solrZookeeperHost = IPStringUtil.getStringAfterStripFromEnd(cfg.getProperty(SOLR_ZOOKEEPER_HOST + "." + (solrZookeeperHosts.size() + 1), null), "/");
            }

            solrCloudDefaultCollection = cfg.getProperty(SOLR_CLOUD_DEFAULT_COLLECTION);
        }

        // bl: encryption is always enabled by default now
        Encryption.INSTANCE.init(Boolean.valueOf(cfg.getProperty(ENABLE_ENCRYPTION, "true")));

        // need to make sure that the NetworkRegistry is available in the servlet context
        cfg.setObject(NETWORK_REGISTRY, this);

        for (ObjectPair<String, String> defaultJspRunnerSetting : DEFAULT_JSP_RUNNER_SETTINGS) {
            String paramName = INTERNAL_JSP_RUNNER_CONFIG_PARAM_PREFIX + defaultJspRunnerSetting.getOne();
            String currentSetting = cfg.getProperty(paramName, null);
            if (currentSetting == null) {
                cfg.setProperty(paramName, defaultJspRunnerSetting.getTwo());
            }
        }
        // bl: turn off "development" in production environments so that Tomcat won't look for changes to the JSP
        // if/when it has to compile. since we precompile JSPs, this shouldn't really matter, but just in case.
        cfg.setProperty(INTERNAL_JSP_RUNNER_CONFIG_PARAM_PREFIX + "development", Boolean.toString(isLocalServer()));
    }

    public static final Collection<Class<? extends PropertySetTypeBase>> PROPERTY_SET_TYPE_DEFS = new LinkedList<>();

    static {
        PROPERTY_SET_TYPE_DEFS.add(SandboxedCommunitySettings.class);
        PROPERTY_SET_TYPE_DEFS.add(GlobalSettings.class);
        PROPERTY_SET_TYPE_DEFS.add(PublicationSettings.class);
    }

    private static void registerDAOImplClasses(PartitionType type) {
        //register the global hibernate classes
        Collection<PartitionDaoConfig<?, ? extends NetworkDAOImpl<? extends DAOObject, ? extends Serializable>>> daoConfigs = PartitionConfig.PARTITION_DAO_CONFIGS.get(type);
        for (PartitionDaoConfig<?, ? extends NetworkDAOImpl<? extends DAOObject, ? extends Serializable>> daoConfig : daoConfigs) {
            NetworkDAOImpl<? extends DAOObject, ? extends Serializable> networkDAOImpl;
            try {
                networkDAOImpl = daoConfig.getDaoClass().newInstance();
            } catch (Throwable t) {
                throw UnexpectedError.getRuntimeException("Failed registering NetworkDAOImpl for class/" + daoConfig.getDaoClass(), t, true);
            }

            assert networkDAOImpl.getPartitionType() == type : "Partition mismatch for " + networkDAOImpl + " expected " + type + " but found " + networkDAOImpl.getPartitionType();
            DAOImpl.registerDAOImpl(networkDAOImpl);
        }
    }

    private static NetworkAnnotationConfiguration createPartitionConfig(PartitionType type) {
        NetworkAnnotationConfiguration cfg = NetworkAnnotationConfiguration.buildNetworkAnnotationConfiguration(type);
        setupHibernateConfig(type, cfg);
        addConfigurationMappingFile(cfg, "/" + type.toString().toLowerCase() + ".hbm.xml");

        return cfg;
    }

    public static void setupHibernateConfig(PartitionType type, NetworkAnnotationConfiguration cfg) {
        //register any package level annotations
        cfg.configure("hibernate.cfg.xml");
        cfg.addPackage("org.narrative.network");
        cfg.setImplicitNamingStrategy(ImplicitNamingStrategyComponentPathImpl.INSTANCE);

        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        Collection<PartitionDaoConfig<?, ? extends NetworkDAOImpl<? extends DAOObject, ? extends Serializable>>> daoConfigs = PartitionConfig.PARTITION_DAO_CONFIGS.get(type);
        for (PartitionDaoConfig<?, ? extends NetworkDAOImpl<? extends DAOObject, ? extends Serializable>> daoConfig : daoConfigs) {
            NetworkDAOImpl<? extends DAOObject, ? extends Serializable> networkDAOImpl = DAOImpl.getDAOFromDAOClass(daoConfig.getDaoClass());
            assert networkDAOImpl.getPartitionType() == type;
            addAnnotatedClass(cfg, networkDAOImpl.getDAOObjectClass());
            testAnnotatedClass(networkDAOImpl.getDAOObjectClass(), contextClassLoader);
            for (Class<? extends DAOObject> descendentClass : networkDAOImpl.getDAOObjectDescendents()) {
                addAnnotatedClass(cfg, descendentClass);
                testAnnotatedClass(descendentClass, contextClassLoader);
            }
        }
    }

    /**
     * Let's fail fast if a class cannot be initialized - initialization error stack traces are eaten by Hibernate and
     * will cost you serious debug time
     */
    private static void testAnnotatedClass(Class aClass, ClassLoader classLoader) {
        try {
            Class.forName(aClass.getName(), true, classLoader);
        } catch (Throwable e) {
            String message = "Error initializing entity class " + aClass.getName();
            //Make sure this gets logged
            logger.error(message, e);
            throw new RuntimeException(message, e);
        }
    }

    private static void addAnnotatedClass(NetworkAnnotationConfiguration cfg, Class<? extends DAOObject> cls) {
        if (Modifier.isFinal(cls.getModifiers()) && cls.isAnnotationPresent(org.hibernate.annotations.Proxy.class)) {
            throw UnexpectedError.getRuntimeException("DAOObject classes that are annotated as proxies should never be final! cls/" + cls.getName());
        }
        cfg.addAnnotatedClass(cls);
    }

    private static void addConfigurationMappingFile(org.hibernate.cfg.Configuration cfg, String filename) {
        //File file;
        try {
            URL url = NetworkRegistry.class.getResource(filename);
            cfg.addURL(url);
        } catch (Exception e) {
            logger.error("Error adding configuration mapping URL for file name " + filename, e);
            throw e;
        }
    }

    public String getTestEmailToUse() {
        return testEmailToUse;
    }

    public String getMysqlBinaryPath() {
        return mysqlBinaryPath;
    }

    public long getServerStartTime() {
        return serverStartTime;
    }

    private long getServletContextLastModifiedTime() {
        return servletContextLastModifiedTime;
    }

    public long getGlobalLastModifiedTime() {
        if (isLocalServer()) {
            return getServerStartTime();
        }
        return getServletContextLastModifiedTime();
    }

    public int getHeartbeatServerPort() {
        return heartbeatServerPort;
    }

    public int getDirectServletHeartbeatServerPort() {
        return directServletHeartbeatServerPort;
    }

    // bl: the following two methods are used in Struts mappings in root-struts.xml for some static images like
    // favicons and apple touch images.

    public AuthRealm getCurrentAuthRealm() {
        return isNetworkContextSet() ? networkContext().getAuthRealm() : ClusterCpAuthRealm.INSTANCE;
    }

    public String getReCaptchaPublicKey() {
        return reCaptchaPublicKey;
    }

    public String getReCaptchaPrivateKey() {
        return reCaptchaPrivateKey;
    }

    public GlobalSettings getGlobalSettings() {
        // bl: if there is no session, then we should use the default GlobalSettings
        if (!PartitionType.GLOBAL.hasCurrentSession()) {
            return DefaultGlobalSettings.DEFAULT_GLOBAL_SETTINGS;
        }
        return GlobalSettingsUtil.getGlobalSettings();
    }

    public Collection<String> getServerNames() {
        return ServerRegistry.INSTANCE.getServerNames();
    }

    private static final Pattern PAGE_SERVER_IDENTIFIER_PATTERN = Pattern.compile("^ps\\d\\d(\\d)");

    public String getDirectServerBaseUrlForServletName(String servletName, boolean isInternalProxy) {
        Matcher matcher = PAGE_SERVER_IDENTIFIER_PATTERN.matcher(servletName);
        if (matcher.matches()) {
            int servletNumber = Integer.valueOf(matcher.group(1));
            int port = 9000 + servletNumber;

            String baseUrl = getClusterCpUrl();
            return baseUrl + ":" + port;
        }
        return getClusterCpUrl();
    }

    public boolean isInstalling() {
        return isInstalling;
    }

    public void setInstalling(boolean installing) {
        isInstalling = installing;
    }

    public boolean isServerInstalled() {
        Configuration cfg = Configuration.getConfiguration();
        //register the JDBC Driver
        try {
            Class.forName(cfg.getProperty(NetworkRegistry.JDBC_DRIVER_CLASS, "com.mysql.jdbc.Driver"));
        } catch (ClassNotFoundException e) {
            logger.warn("Could not load db driver");
            return false;
        }

        String globalPartName = "global";//PartitionType.GLOBAL.toString().toLowerCase();
        String server = cfg.getProperty(globalPartName + ".server", "localhost");
        String name = cfg.getProperty(globalPartName + ".database", globalPartName);
        String user = cfg.getProperty(globalPartName + ".username", name + "_user");
        String password = cfg.getProperty(name + ".password", "");

        String url = PersistenceUtil.MySQLUtils.getJDBCURL(server, name, user, password);

        Connection con;
        try {
            con = DriverManager.getConnection(url);
        } catch (SQLException e) {
            logger.warn("Could not connect to global db driver", e);
            return false;
        }

        try {
            Statement stmt = con.createStatement();
            stmt.execute("select count(*) from " + AppliedPatch.class.getSimpleName());
        } catch (SQLException e) {
            logger.warn("Could not query AppliedPatch table", e);
            return false;
        } finally {
            PersistenceUtil.closeConnection(con);
        }

        return true;
    }

    public boolean isUsesNeoMainNet() {
        // bl: let's use NEO's MainNet for both production and staging now.
        return isProductionServer() || isStagingServer();
    }

    public String getNeoscanBaseUrl() {
        if(isUsesNeoMainNet()) {
            return "https://neoscan.io";
        }
        return "https://neoscan-testnet.io";
    }

    public String getNeotrackerBaseUrl() {
        if(isUsesNeoMainNet()) {
            return "https://neotracker.io";
        }
        // bl: this doesn't work anymore, but putting it here anyway
        return "http://testnet.neotracker.io";
    }

    public String getNewEconoLabBaseUrl() {
        if(isUsesNeoMainNet()) {
            return "https://scan.nel.group";
        }
        return "https://scan.nel.group/test";
    }

    public NarrativeProperties getNarrativeProperties() {
        return StaticConfig.getBean(NarrativeProperties.class);
    }

    public String getReferenceIdFromException(Throwable exception) {
        StringBuilder referenceId = new StringBuilder();
        referenceId.append("[");
        referenceId.append(Debug.getHashCodeFromException(exception));
        referenceId.append("][");
        referenceId.append(Debug.getRootCauseClassHashCode(exception));
        referenceId.append("]");
        referenceId.append(getVersion());
        referenceId.append("-");
        referenceId.append(getClusterId());
        referenceId.append("-");
        referenceId.append(getServletName());
        return referenceId.toString();
    }

    public void sendDevOpsStatusEmail(String subject, String body) {
        sendDevOpsStatusEmail(subject, body, true);
    }

    public void sendDevOpsStatusEmail(String subject, String body, boolean sendOnSuccessOrError) {
        String emailAddress = getNarrativeProperties().getCluster().getDevOpsEmailAddress();
        body += "\n\nSent from app server: " + getServletName();
        String fromDisplayName = "Narrative Alerts (" + getClusterId() + ")";
        NetworkMailUtil.sendJavaCreatedEmail(null, NarrativeAuthZoneMaster.INSTANCE.getReplyToEmailAddress(), fromDisplayName, null, Collections.singleton(emailAddress), Collections.singleton("DevOps"), subject, body, true, false, sendOnSuccessOrError);
    }
}
