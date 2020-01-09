package org.narrative.network.core.system;

import org.narrative.network.shared.jsptags.StaticImageTag;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;

/**
 * Initialize display resources that are needed at servlet initialization. Due to chicken & egg issues with
 * startup and the fact that wordlets are initialized as a patch, we need to do this initialization both
 * in the patch framework (after wordlets are initialized) and in the standard NetworkRegistry initialization
 * (for scenarios where the patches don't run at startup).
 * <p>
 * Date: 4/17/15
 * Time: 10:13 AM
 *
 * @author brian
 */
public class InitDisplayResources extends GlobalTaskImpl<Object> {
    public InitDisplayResources() {
        super(false);
    }

    @Override
    protected Object doMonitoredTask() {
        // bl: always need to initialize the StaticImageTag.  it relies on some wordlets, so we also need to make
        // sure that a NetworkContext is set (so running in a global task).
        // nb. need to do this even for non-webapps since we now have some command-line apps
        // (such as the importer) which generate file output from JSPs that will rely on static images.
        // nb. need to do prior to running the utility server init or else we might not initialize all images
        // prior to starting the durable task handler, which may try to send emails, which ultimately can
        // cause NPEs.
        StaticImageTag.initStaticImageMap();
        return null;
    }
}
