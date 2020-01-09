package org.narrative.network.customizations.narrative.services;

import org.narrative.network.core.quartz.NetworkJob;
import org.narrative.network.core.settings.global.GlobalSettings;
import org.narrative.network.core.settings.global.services.GlobalSettingsUtil;
import org.narrative.network.shared.util.NetworkLogger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.math.BigDecimal;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 7/27/18
 * Time: 2:32 PM
 */
@DisallowConcurrentExecution
public class CurrentNrveUsdValueCachingJob extends NetworkJob {
    private static final NetworkLogger logger = new NetworkLogger(CurrentNrveUsdValueCachingJob.class);

    @Override
    protected void executeJob(JobExecutionContext context) throws JobExecutionException {
        BigDecimal oldNrveUsdPrice = GlobalSettingsUtil.getGlobalSettings().getNrveUsdPrice();
        GlobalSettings globalSettings = GlobalSettingsUtil.getGlobalSettingsForWrite();
        // jw: calling this method will fetch and cache the current nrve value, and the neurons for new niche purchases.
        globalSettings.refreshCurrentNrveCachedValues();
        if(logger.isInfoEnabled()) logger.info("Updated NRVE-USD from " + oldNrveUsdPrice + " to " + globalSettings.getNrveUsdPrice());
    }
}
