package org.narrative.network.customizations.narrative.posts.services;

import org.narrative.common.persistence.OID;
import org.narrative.common.util.html.HTMLParser;
import org.narrative.network.core.composition.base.Composition;
import org.narrative.network.core.composition.base.services.DeleteCompositionConsumer;
import org.narrative.network.core.content.base.Content;
import org.narrative.network.core.quartz.NetworkJob;
import org.narrative.network.shared.tasktypes.CompositionTaskImpl;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;
import org.narrative.network.shared.tasktypes.TaskRunner;
import org.narrative.network.shared.util.NetworkLogger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Date: 2019-01-16
 * Time: 12:26
 *
 * @author jonmark
 */
@DisallowConcurrentExecution
public class CleanupEmptyDraftPostsJob extends NetworkJob {
    private static final NetworkLogger logger = new NetworkLogger(CleanupEmptyDraftPostsJob.class);

    @Deprecated // Quartz only
    public CleanupEmptyDraftPostsJob() { }

    @Override
    protected void executeJob(JobExecutionContext context) throws JobExecutionException {
        if (logger.isDebugEnabled()) {
            logger.debug("Starting to process stale empty Narrative Post drafts.");
        }

        // jw: let's look for any empty drafts saved between 7 and 14 days ago. That way we will not keep processing the
        //     same drafts that only have visible HTML in their bodies forever.
        Instant savedBefore = Instant.now().minus(7, ChronoUnit.DAYS);
        Instant savedAfter = Instant.now().minus(14, ChronoUnit.DAYS);

        // jw: first, let's just get all contentOids that are eligible for deletion on a cursory level.
        List<OID> contentOids = Content.dao().getEmptyNarrativePostDraftOids(savedBefore, savedAfter);

        // jw: short out if we don't have any content to process
        if (contentOids.isEmpty()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Shorting out since there are no stale NarrativePost drafts.");
            }

            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Preparing to process "+contentOids.size()+" stale NarrativePost drafts.");
        }

        int processedContentOidCount = 0;

        // jw: iterate through all chunks, and process each chunk atomically.
        for (OID contentOid : contentOids) {
            deleteStaleEmptyDraft(contentOid);

            processedContentOidCount++;
            if (logger.isDebugEnabled() && processedContentOidCount % 50 == 0) {
                logger.debug("Processed " +processedContentOidCount+ " out of "+contentOids.size()+" stale NarrativePost drafts.");
            }
        }
    }

    private void deleteStaleEmptyDraft(OID contentOid) {
        TaskRunner.doRootGlobalTask(new GlobalTaskImpl<Object>() {
            @Override
            protected Object doMonitoredTask() {
                Content content = Content.dao().get(contentOid);

                boolean delete = getNetworkContext().doCompositionTask(content.getCompositionPartition(), new CompositionTaskImpl<Boolean>() {
                    @Override
                    protected Boolean doMonitoredTask() {
                        // jw: before we delete the content let's make sure that the body does not have any visible
                        //     content within it. We need to do this because the extract could be empty because
                        //     the draft only contains a video or image.
                        Composition composition = content.getComposition();

                        return !HTMLParser.doesHtmlFragmentContainVisibleContent(composition.getBody());
                    }
                });

                if (delete) {
                    getNetworkContext().doAreaTask(content.getArea(), new DeleteCompositionConsumer(content));

                } else if (logger.isDebugEnabled()) {
                    logger.debug("NarrativePost draft/"+contentOid+" has visible HTML in its body, skipping.");
                }

                return null;
            }
        });
    }
}
