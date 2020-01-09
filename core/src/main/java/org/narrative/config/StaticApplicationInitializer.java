package org.narrative.config;

import ch.qos.logback.classic.LoggerContext;
import org.narrative.common.util.IPUtil;
import org.narrative.network.core.cluster.setup.NetworkSetup;
import org.narrative.network.core.system.HeartbeatServer;
import org.narrative.network.core.system.NetworkRegistry;
import org.narrative.network.shared.servlet.StaticFilterUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Do static initialization part 2 here.  This gives us a component we can @DependsOn to force ordered start up when using
 * non-Spring managed resources.
 */
@Slf4j
@Component("staticApplicationInitializer")
@DependsOn("servletConfigInitializer")
public class StaticApplicationInitializer implements ApplicationListener<ApplicationContextEvent> {

    @EventListener(ApplicationReadyEvent.class)
    public void finalizeStartup() {
        // bl: now that everything in spring boot has initialized, let's do our app startup.
        if(log.isInfoEnabled()) log.info("STARTING " + StaticFilterUtils.getStartupString());
        try {
            if(NetworkRegistry.getInstance().isServerInstalled()){
                //configure the network
                NetworkSetup.doServletSetup();
            } else {
                throw new RuntimeException("Application is not configured. Run NetworkInstall.");
            }
        } catch(RuntimeException e) {
            log.error("FAILED " + StaticFilterUtils.getStartupString(), e);
            throw e;
        }

        // bl: we're fully initialized, so let's start up the heartbeat ports since we should be able to start taking requests.
        HeartbeatServer.INSTANCE.startServer(NetworkRegistry.getInstance().getHeartbeatServerPort());
        HeartbeatServer.DIRECT_SERVLET.startServer(NetworkRegistry.getInstance().getDirectServletHeartbeatServerPort());

        if(log.isInfoEnabled()) log.info("FINISHED " + StaticFilterUtils.getStartupString());

        IPUtil.EndOfX.endOfAppComing.addRunnable("000-ShutdownHeartbeatServers", () -> {
            // bl: the first thing to do when shutting down is to stop the heartbeat servers!
            // telling the server to stop if it's not running does no harm
            for (HeartbeatServer heartbeatServer : HeartbeatServer.values()) {
                heartbeatServer.stopServerSafely();
            }
        });
    }

    @Override
    public void onApplicationEvent(ApplicationContextEvent event) {
        if(event instanceof ContextStoppedEvent) {
            onContextStopped();
            return;
        }
        if(event instanceof  ContextClosedEvent) {
            onContextClosed();
            return;
        }
    }

    /**
     * Wait until Spring has completely shut down before killing logger output
     */
    private void onContextStopped() {
        ((LoggerContext) LoggerFactory.getILoggerFactory()).stop();
    }

    /**
     * As soon as the Spring ApplicationContext is shut down, we want to start our normal shutdown process in order
     * to ensure that we get everything shut down before Spring starts destroying beans that outstanding
     * jobs might need in order to wrap up processing (e.g. Quartz jobs relying on the database/Redis).
     */
    private void onContextClosed() {
        if (log.isInfoEnabled()) log.info("STARTING NARRATIVE SERVLET SHUTDOWN - " + StaticFilterUtils.getLogSuffix());
        // do the shutdown tasks. note that the HeartbeatServer has already been stopped as part of GStrutsPrepareAndExecuteFilter.
        try {
            IPUtil.onEndOfApp();
        } finally {
            if (log.isInfoEnabled()) {
                log.info("NARRATIVE SERVLET SHUTDOWN COMPLETE - " + StaticFilterUtils.getLogSuffix());
            }
        }
    }
}
