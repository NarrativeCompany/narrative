package org.narrative.network.shared.services;

import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.OIDGenerator;
import org.narrative.common.persistence.hibernate.GSession;
import org.narrative.common.util.Configuration;
import org.narrative.common.util.IPDateUtil;
import org.narrative.common.util.IPStringUtil;
import org.narrative.common.util.IPUtil;
import org.narrative.common.util.UnexpectedError;
import org.narrative.common.web.jsp.InternalJSPRunner;
import org.narrative.config.JacksonConfiguration;
import org.narrative.config.ValidationConfig;
import org.narrative.config.cache.RedissonConfig;
import org.narrative.config.properties.NarrativeProperties;
import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.area.base.services.CreateCommunityTask;
import org.narrative.network.core.cluster.partition.Partition;
import org.narrative.network.core.cluster.partition.PartitionConnectionPool;
import org.narrative.network.core.cluster.partition.PartitionType;
import org.narrative.network.core.cluster.setup.NetworkSetup;
import org.narrative.network.core.narrative.rewards.services.BootstrapRewardPeriodsTask;
import org.narrative.network.core.narrative.rewards.services.BootstrapTokenMintWalletTask;
import org.narrative.network.core.narrative.wallet.services.BootstrapNeoWalletsTask;
import org.narrative.network.core.propertyset.base.PropertySet;
import org.narrative.network.core.propertyset.base.services.InstallDefaultPropertySets;
import org.narrative.network.core.quartz.services.InstallSystemCronJobs;
import org.narrative.network.core.quartz.services.QuartzInstaller;
import org.narrative.network.core.quartz.services.QuartzJobScheduler;
import org.narrative.network.core.settings.global.services.translations.TranslationRegistry;
import org.narrative.network.core.system.NetworkRegistry;
import org.narrative.network.core.system.NetworkVersion;
import org.narrative.network.core.user.NarrativeAuthZoneMaster;
import org.narrative.network.core.versioning.services.CreatePatchRunnerLock;
import org.narrative.network.core.versioning.services.PatchRunner;
import org.narrative.network.core.versioning.services.UpdateFunctions;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;
import org.narrative.network.shared.tasktypes.TaskRunner;
import org.narrative.network.shared.util.NetworkJspRunner;
import org.narrative.network.shared.util.NetworkLogger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.solr.SolrAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Dec 4, 2005
 * Time: 12:24:22 AM
 */
@SpringBootApplication(exclude = {
        HibernateJpaAutoConfiguration.class,
        DataSourceAutoConfiguration.class,
        ErrorMvcAutoConfiguration.class,
        SolrAutoConfiguration.class
})
@Import(value = {NarrativeProperties.class, NetworkJspRunner.class, InternalJSPRunner.class, JacksonConfiguration.class, RedissonConfig.class, ValidationConfig.class})
public class NetworkInstall {
    private static final NetworkLogger logger = new NetworkLogger(NetworkInstall.class);

    public static final String MYSQL_SERVER = "mysql.server";
    public static final String MYSQL_ROOT_PASSWORD = "mysql.root.password";

    public static final String NARRATIVE_ADMIN_PASSWORD = "admin.password";
    public static final String NARRATIVE_ADMIN_DISPLAY_NAME = "admin.displayName";
    public static final String NARRATIVE_ADMIN_EMAIL = "admin.email";

    public static void main(String[] args) {
        SpringApplication.run(NetworkInstall.class, args);
    }

    @Bean
    public RunNetworkInstall runNetworkInstall(ApplicationContext applicationContext) {
        return new RunNetworkInstall(applicationContext);
    }

    private static class RunNetworkInstall implements CommandLineRunner {
        private final ApplicationContext applicationContext;

        public RunNetworkInstall(ApplicationContext applicationContext) {
            this.applicationContext = applicationContext;
        }

        @Override
        public void run(String... args) throws IOException {
            if (args == null || args.length < 2) {
                System.out.println("usage: NetworkInstall [install|${PartitionTypeToCreate}] <path-to-install.properties> [drop-existing-database:TRUE!]");
                return;
            }

            try {

                NetworkVersion.INSTANCE.init("1.0.0", 1, "install", "install");
                //load configuration
                Properties prop = new Properties();
                String propertiesFile;
                if (!IPStringUtil.isEmpty(args[1])) {
                    propertiesFile = args[1];
                } else {
                    propertiesFile = "src/main/config/install.properties";
                }
                prop.load(new FileInputStream(propertiesFile));

                Configuration installConfiguration = new Configuration(prop);

                NetworkSetup.doSetup(applicationContext, true);

                PartitionType partitionTypeToCreate = null;
                String action = args[0];
                if (!"install".equalsIgnoreCase(action)) {
                    try {
                        partitionTypeToCreate = PartitionType.valueOf(action);
                    } catch (Throwable t) {
                        throw UnexpectedError.getRuntimeException("Must supply a valid PartitionType to create! " + action, t);
                    }
                }

                String drop = null;
                if (args.length > 2) {
                    drop = args[2];
                    if (!drop.equalsIgnoreCase("TRUE!")) {
                        System.out.println("Drop argument must match the following string exactly:  TRUE!");
                        return;
                    }
                }

                if (partitionTypeToCreate == null) {
                    try {
                        install(drop, installConfiguration);
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                } else {
                    if (!partitionTypeToCreate.isSingleton()) {
                        throw UnexpectedError.getRuntimeException("Can only create singleton partitions with this mechanism. partitionType/" + partitionTypeToCreate);
                    }
                    partitionTypeToCreate.getSingletonPartition().createPartitionDatabase(installConfiguration.getProperty(MYSQL_SERVER), installConfiguration.getProperty(MYSQL_ROOT_PASSWORD));
                }
            } finally {
                IPUtil.onEndOfApp();
            }
        }
    }

    /**
     * Installs the network, DROPPING! databases first.
     *
     * @param dropDatabasesIfExisting MUST be the literal string "TRUE!".  For extra saftey.
     */
    public static void install(final String dropDatabasesIfExisting, final Configuration installConfiguration) {

        // first, create the singleton partition databases
        for (PartitionType partitionType : PartitionType.getSingletonPartitionTypes()) {
            assert partitionType.getDatabaseType().isMysql() : "Only MySQL DBs supported now!";

            Partition partition = partitionType.getSingletonPartition();
            String server = installConfiguration.getProperty(MYSQL_SERVER);
            String rootPw = installConfiguration.getProperty(MYSQL_ROOT_PASSWORD);

            //create the partition database
            if ("TRUE!".equals(dropDatabasesIfExisting)) {
                partition.dropPartitionDatabase(server, rootPw, true);
            }

            // bl: if there was no password supplied, then we should generate a random password
            if (isEmpty(partition.getPassword())) {
                partition.setPassword(getRandomPassword());
            }

            partition.createPartitionDatabase(server, rootPw);
        }

        // once all of the singleton partition databases have been created, we can add cross-partition permissions
        // and install the database tables
        for (PartitionType partitionType : PartitionType.getSingletonPartitionTypes()) {
            Partition partition = partitionType.getSingletonPartition();
            String server = partition.getServer();

            if (server != null) {
                PartitionConnectionPool.registerPartitionConnectionPool(partition);

                partition.createDatabaseTables(false, false);
            }
        }

        TaskRunner.doRootGlobalTask(new GlobalTaskImpl<Object>() {
            protected Object doMonitoredTask() {

                GSession globalSession = getNetworkContext().getGlobalSession();
                //save the singleton partitions if needed
                for (PartitionType partitionType : PartitionType.getSingletonPartitionTypes()) {
                    Partition partition = partitionType.getSingletonPartition();
                    String server = partition.getServer();
                    if (server != null) {
                        Partition.dao().save(partition);
                    }
                }

                // bl: now that the databases are installed, let's install the functions.
                // we used to do this as part of createDatabaseTables since functions sql was considered install sql.
                // we can't do that anymore though due to dependencies between PartitionTypes on each other in our functions.
                new UpdateFunctions().applyPatch(PartitionType.GLOBAL.getSingletonPartition(), new Properties());

                // bl: now that we've installed the translation, initialize the TranslationRegistry so that
                // the ResourceBundles are ready to go (will be used below when setting up the SystemRole for tasks).
                getNetworkContext().doGlobalTask(new TranslationRegistry.InitTask());

                // bl: also populate the property set tables with defaults
                getNetworkContext().doGlobalTask(new InstallDefaultPropertySets());

                //bk: create quartz tables
                getNetworkContext().doGlobalTask(new QuartzInstaller());

                // bl: once we have created quartz tables, start up the scheduler
                QuartzJobScheduler.GLOBAL.start();

                //create any composition partitions needed
                createPartitionFromConfig("comp", installConfiguration, dropDatabasesIfExisting, PartitionType.COMPOSITION);

                globalSession.flushSession();

                // bl: now that the realm and composition dbs are installed, let's install the functions.
                // we used to do this as part of createDatabaseTables since functions sql was considered install sql.
                // we can't do that anymore though due to dependencies between PartitionTypes on each other in our functions.
                new UpdateFunctions().applyPatch(PartitionType.GLOBAL.getSingletonPartition(), new Properties());

                return null;
            }
        });

        TaskRunner.doRootGlobalTask(new CreatePatchRunnerLock());

        // bl: do this in its own session once all of the partitions have been created in the previous transaction.
        TaskRunner.doRootGlobalTask(new GlobalTaskImpl<Object>() {
            protected Object doMonitoredTask() {
                //Since this is an install, we can record all the patches as having been run
                getNetworkContext().doGlobalTask(new PatchRunner(true, true));
                return null;
            }
        });

        // bl: sleep for 10 seconds to give enough time for the rollup stats to start (and probably finish).
        // bk: we could add a listener to the above task with wait/notify but for now this is ok
        IPUtil.uninterruptedSleep(10 * IPDateUtil.SECOND_IN_MS);

        // bl: now that the install process is complete, turn the installing flag off
        NetworkRegistry.getInstance().setInstalling(false);

        // bl: must initialize the default PropertySets now so that we can proceed to create the platform Area!
        // bl: initialize the default PropertySets so that they are cached for the life of the servlet
        TaskRunner.doRootGlobalTask(new GlobalTaskImpl<Object>() {
            @Override
            protected Object doMonitoredTask() {
                PropertySet.dao().initializeDefaultPropertSets();
                return null;
            }
        });

        // Create Narrative community
        OID areaOid = TaskRunner.doRootGlobalTask(new GlobalTaskImpl<OID>() {
            @Override
            protected OID doMonitoredTask() {
                CreateCommunityTask task = new CreateCommunityTask(NarrativeAuthZoneMaster.NARRATIVE_NAME);

                task.setEmailAddress(installConfiguration.getProperty(NARRATIVE_ADMIN_EMAIL));
                // bl: mark the owner's email as verified to ease onboarding
                task.setEmailVerified(true);
                task.setPassword(installConfiguration.getProperty(NARRATIVE_ADMIN_PASSWORD));
                task.setDisplayName(installConfiguration.getProperty(NARRATIVE_ADMIN_DISPLAY_NAME));
                task.setSendNewUserEmail(false);

                Area area = getNetworkContext().doGlobalTask(task);

                // bl: initialize encryption
                getNetworkContext().doAreaTask(area, new AreaTaskImpl<Object>() {
                    @Override
                    protected Object doMonitoredTask() {
                        getAreaContext().getAreaRlm().getSandboxedCommunitySettings().initEncryption();
                        return null;
                    }
                });

                // bl: bootstrap the NeoWallets
                getNetworkContext().doAreaTask(area, new BootstrapNeoWalletsTask());

                // bl: bootstrap the token mint wallet
                getNetworkContext().doAreaTask(area, new BootstrapTokenMintWalletTask());

                return area.getOid();
            }
        });

        // bl: bootstrap the RewardPeriod and ProratedMonthRevenue records.
        // bl: do this in a separate transaction so that the internal transaction handling will work.
        // if we did it in the transaction above, isolated transactions don't work because the Area hasn't
        // even been committed to the database yet.
        // bl: set that we're installing so that IssueRewardsStepProcessor doesn't send emails unnecessarily for months that are processed without distributing any rewards.
        NetworkRegistry.getInstance().setInstalling(true);
        try {
            TaskRunner.doRootAreaTask(areaOid, new BootstrapRewardPeriodsTask());
        } finally {
            NetworkRegistry.getInstance().setInstalling(false);
        }

        // bl: need to initialize the system cron jobs on new installs, as well.
        // bl: needs to happen after we've installed the Area and bootstrapped the reward periods so that there aren't
        // errors when these jobs get run for the first time.
        InstallSystemCronJobs.initializeSystemCronJobs();

        if (logger.isInfoEnabled()) {
            logger.info("Install complete!");
        }
    }

    private static void createPartitionFromConfig(String prefix, Configuration installConfiguration, String dropDatabasesIfExisting, PartitionType type) {
        String server = installConfiguration.getProperty(MYSQL_SERVER);
        String rootPw = installConfiguration.getProperty(MYSQL_ROOT_PASSWORD);
        String prefixDot = prefix + ".";
        for (int i = 1; i < 1000; i++) {
            String partitionServer = installConfiguration.getProperty(prefixDot + i + ".server", null);
            if (isEmpty(partitionServer)) {
                continue;
            }

            String databaseName = installConfiguration.getProperty(prefixDot + i + ".database", prefix + i);
            String user = installConfiguration.getProperty(prefixDot + i + ".user", prefix + i + "_user");
            String password = installConfiguration.getProperty(prefixDot + i + ".password", "");
            if (isEmpty(password)) {
                password = getRandomPassword();
            }
            String parameters = installConfiguration.getProperty(prefixDot + i + ".parameters", PartitionType.GLOBAL.getSingletonPartition().getParameters());

            Partition emptyPartition = new Partition(type);
            emptyPartition.setOid(OIDGenerator.getNextOID());
            emptyPartition.setServer(partitionServer);
            emptyPartition.setDatabaseName(databaseName.trim());
            emptyPartition.setUsername(user);
            emptyPartition.setPassword(password);
            emptyPartition.setParameters(parameters);

            Partition.dao().save(emptyPartition);

            if ("TRUE!".equals(dropDatabasesIfExisting)) {
                emptyPartition.dropPartitionDatabase(server, rootPw, true);
            }

            emptyPartition.createPartitionDatabase(server, rootPw);

            PartitionConnectionPool.registerPartitionConnectionPool(emptyPartition);

            emptyPartition.createDatabaseTables(false, false);
        }
    }

    private static String getRandomPassword() {
        // bl: let's use an MD5 for the password based off of a randomized password
        return IPStringUtil.getMD5DigestFromString(IPUtil.generatePassword(14, 16));
    }

}
