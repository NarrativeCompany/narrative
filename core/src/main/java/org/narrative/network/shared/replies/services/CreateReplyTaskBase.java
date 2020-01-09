package org.narrative.network.shared.replies.services;

import org.narrative.common.persistence.OID;
import org.narrative.common.util.ValidationHandler;
import org.narrative.network.core.area.base.services.ItemHourTrendingStatsManager;
import org.narrative.network.core.cluster.partition.PartitionGroup;
import org.narrative.network.core.composition.base.CompositionConsumer;
import org.narrative.network.core.composition.base.Reply;
import org.narrative.network.core.content.base.Content;
import org.narrative.network.core.content.base.FileData;
import org.narrative.network.core.search.services.ReplyIndexRunnable;
import org.narrative.network.core.security.area.base.AreaRole;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 9/23/11
 * Time: 12:35 PM
 *
 * @author brian
 * <p>
 * jw: We need this abstract class to track all of the shared objects and set things up a bit.  The order of events for
 * actually running the CreateCompositionReplyTask changes between Content and Clip, so this class does not even attempt
 * to implement doMonitoredTask.
 */
public abstract class CreateReplyTaskBase<T extends CompositionConsumer> extends AreaTaskImpl<OID> {

    protected final T compositionConsumer;
    protected final Reply reply;

    private final CreateCompositionReplyTask task;

    protected CreateReplyTaskBase(T compositionConsumer, Reply reply, List<? extends FileData> attachments, String originalBody) {
        this.compositionConsumer = compositionConsumer;
        this.reply = reply;
        this.task = new CreateCompositionReplyTask(compositionConsumer, reply, attachments);

        assert reply.isNew() == isEmpty(originalBody) : "Should never provide a originalBody for a new Reply, and should always have a original body for existing replies!";
        task.setOriginalBody(originalBody);
    }

    @Override
    protected void validate(ValidationHandler validationHandler) {
        task.setValidationHandlerAndValidate(validationHandler);
    }

    @Override
    protected OID doMonitoredTask() {
        AreaRole areaRole = getAreaContext().getAreaRole();

        // bl: always sync the stats.  lastUpdateDatetime may have changed.
        //increment the reply counts
        if (compositionConsumer.getCompositionType().isSupportsStats()) {
            compositionConsumer.getStatsForUpdate().syncStats(reply.getComposition().getCompositionStats());
        }

        if (reply.isNew()) {
            if (compositionConsumer.getCompositionType().isContent() && compositionConsumer.getContentType().isSupportsTrendingStats()) {
                Content content = (Content) compositionConsumer;
                // bl: only record the reply if this author has had fewer than 5 replies in the past 24 hours
                int authorReplyCountInPastDay = Reply.dao().getReplyCountForAuthorAndConsumerAfterDate(reply.getUser(), compositionConsumer, Instant.now().minus(1, ChronoUnit.DAYS));
                // bl: this query should always return at least 1 for the reply that was just made. thus, we need
                // the check for 5 comments to be inclusive.
                if (authorReplyCountInPastDay <= 5) {
                    ItemHourTrendingStatsManager.recordContentReply(getNetworkContext().getPrimaryRole(), content);
                }
            }
        }

        // jw: lets send the message to re-index this reply since its area changed (assuming that it corresponds to a searchable type)
        PartitionGroup.addEndOfPartitionGroupRunnable(new ReplyIndexRunnable(reply.getOid(), compositionConsumer.getCompositionPartition().getOid()));

        return reply.getOid();
    }

    protected CreateCompositionReplyTask getCreateReplyTask() {
        return task;
    }
}
