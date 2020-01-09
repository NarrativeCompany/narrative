package org.narrative.network.core.cluster.actions;

import org.narrative.common.core.services.interceptors.BypassHtmlDisable;
import org.narrative.config.StaticConfig;
import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.area.base.services.ProcessItemHourTrendingStatsJob;
import org.narrative.network.core.area.base.services.RemoveStaleItemHourTrendingStatsJob;
import org.narrative.network.core.content.base.services.CalculateTrendingContentJob;
import org.narrative.network.core.fileondisk.base.services.CleanUpOldFileDataJob;
import org.narrative.network.core.quartz.AreaJob;
import org.narrative.network.core.quartz.NetworkJob;
import org.narrative.network.core.quartz.services.QuartzJobScheduler;
import org.narrative.network.core.search.IndexType;
import org.narrative.network.core.search.services.AuditSearchIndexMissingItems;
import org.narrative.network.core.search.services.IndexHandlerIndexRebuildJob;
import org.narrative.network.core.search.services.IndexHandlerJobBase;
import org.narrative.network.core.search.services.OptimizeIndexes;
import org.narrative.network.shared.services.ConfirmationMessage;
import org.narrative.network.shared.services.NetworkException;
import org.narrative.network.shared.struts.NetworkResponses;
import org.narrative.shared.redisson.management.RedissonObjectManager;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: barry
 * Date: Jan 10, 2007
 * Time: 2:05:49 PM
 */
public class TriggerTaskAction extends ClusterAction {

    public static final String ACTION_NAME = "triggerTask";

    private String areaOidOrDomain;
    private List<IndexType> indexTypes;

    private Integer month;
    private Integer year;

    private String customString;

    public String input() throws Exception {

        return INPUT;
    }

    // UTILITY SERVLET TASKS GO HERE

    public String calculateTrendingContent() throws Exception {
        return runTaskNow(CalculateTrendingContentJob.class);
    }

    public String removeStaleItemHourTrendingStatsHandler() throws Exception {
        return runTaskNow(RemoveStaleItemHourTrendingStatsJob.class);
    }

    public String rebuildIndexes() throws Exception {
        return runIndexHandlerTaskNow(IndexHandlerIndexRebuildJob.class);
    }

    public String rebuildIndexesForArea() throws Exception {
        return runIndexHandlerTaskNow(IndexHandlerIndexRebuildJob.class);
    }

    public String auditIndexMissingItems() throws Exception {
        return runIndexHandlerTaskNow(AuditSearchIndexMissingItems.class);
    }

    public String optimizeIndexes() throws Exception {
        return runTaskNow(OptimizeIndexes.class);
    }

    // NON-UTILITY SERVLET TASKS GO HERE

    public String clearRedissonCache() {
        RedissonObjectManager redissonObjectManager = StaticConfig.getBean(RedissonObjectManager.class);
        redissonObjectManager.clearLocalCache(customString);
        redissonObjectManager.clearRedisCache(customString);
        setConfirmationMessageForSubsequentRequest(new ConfirmationMessage("Redisson cache '" + customString + "' cleared!"));
        return NetworkResponses.redirectResponse();
    }

    public String clearAllRedissonCaches() {
        RedissonObjectManager redissonObjectManager = StaticConfig.getBean(RedissonObjectManager.class);
        redissonObjectManager.clearAllCaches();
        setConfirmationMessageForSubsequentRequest(new ConfirmationMessage("All Redisson caches cleared!"));
        return NetworkResponses.redirectResponse();
    }

    public String flushHourStats() throws Exception {
        return runLocalTaskNow(ProcessItemHourTrendingStatsJob.class);
    }

    public String runCleanupOldFiles() throws Exception {
        return runLocalTaskNow(CleanUpOldFileDataJob.class);
    }

    private String runLocalTaskNow(Class<? extends NetworkJob> clz) {
        return runLocalTaskNow(clz, null);
    }

    private String runLocalTaskNow(Class<? extends NetworkJob> clz, Area area) {
        return runLocalTaskNow(clz, area, null);
    }

    private String runLocalTaskNow(Class<? extends NetworkJob> clz, Area area, JobDataMap extraJobData) {
        JobBuilder jobBuilder = createOneTimeJobDetail(clz);
        if (exists(area)) {
            AreaJob.addAreaToJobDataMap(area, jobBuilder);
        }
        if (extraJobData != null) {
            jobBuilder.usingJobData(extraJobData);
        }
        QuartzJobScheduler.LOCAL.schedule(jobBuilder);
        addConfirmationMessage(clz.getSimpleName() + " triggered.");
        return NetworkResponses.redirectResponse();
    }

    private JobBuilder createOneTimeJobDetail(Class<? extends NetworkJob> clz) {
        return QuartzJobScheduler.createRecoverableJobBuilder(clz, "-OneTime");
    }

    private String runIndexHandlerTaskNow(Class<? extends IndexHandlerJobBase> clz) {
        JobBuilder jobBuilder = createOneTimeJobDetail(clz);
        IndexHandlerJobBase.storeIndexTypesSet(jobBuilder, getIndexTypesSet());
        QuartzJobScheduler.LOCAL.schedule(jobBuilder);
        addConfirmationMessage(clz.getSimpleName() + " triggered.");
        return NetworkResponses.redirectResponse();
    }

    private String runTaskNow(Class<? extends NetworkJob> clz) {
        return runTaskNow(clz, null);
    }

    private String runTaskNow(Class<? extends NetworkJob> clz, Area area) {
        return runTaskNow(clz, area, null);
    }

    private String runTaskNow(Class<? extends NetworkJob> clz, Area area, JobDataMap extraJobData) {
        JobBuilder jobBuilder = createOneTimeJobDetail(clz);
        if (exists(area)) {
            AreaJob.addAreaToJobDataMap(area, jobBuilder);
        }
        if (extraJobData != null) {
            jobBuilder.usingJobData(extraJobData);
        }
        QuartzJobScheduler.GLOBAL.schedule(jobBuilder);
        addConfirmationMessage(clz.getSimpleName() + " triggered.");
        return NetworkResponses.redirectResponse();
    }

    private void addConfirmationMessage(String message) {
        setConfirmationMessageForSubsequentRequest(new ConfirmationMessage("Task Output: " + message, true));
    }

    public void setAreaOidOrDomain(String areaOidOrDomain) {
        this.areaOidOrDomain = areaOidOrDomain;
    }

    public List<IndexType> getIndexTypes() {
        return indexTypes;
    }

    public void setIndexTypes(List<IndexType> indexTypes) {
        this.indexTypes = indexTypes;
    }

    public Set<IndexType> getIndexTypesSet() {
        Set<IndexType> ret = new HashSet<IndexType>();
        if (indexTypes != null && !indexTypes.isEmpty()) {
            for (IndexType indexType : indexTypes) {
                if (indexType != null) {
                    ret.add(indexType);
                }
            }
        }
        if (ret.isEmpty()) {
            throw new NetworkException("You must select one or more index types to apply the task to!");
        }
        return ret;
    }

    public List<IndexType> getAllIndexTypes() {
        return IndexType.getAllIndexTypes();
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    @BypassHtmlDisable
    public void setCustomString(String customString) {
        this.customString = customString;
    }

    @Override
    public String getSubMenuResource() {
        return CreateHQLQueryResultsAction.CLUSTER_ADMIN_MENU_RESOURCE;
    }

    @Override
    public String getNestedSubMenuResource() {
        return ACTION_NAME;
    }
}
