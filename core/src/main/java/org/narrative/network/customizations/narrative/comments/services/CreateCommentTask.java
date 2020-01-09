package org.narrative.network.customizations.narrative.comments.services;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.composition.base.CompositionConsumer;
import org.narrative.network.core.composition.base.Reply;
import org.narrative.network.core.mentions.SendNewMentionsNotificationEmailJob;
import org.narrative.network.shared.replies.services.CreateReplyTaskBase;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 2/28/18
 * Time: 10:08 AM
 */
public class CreateCommentTask extends CreateReplyTaskBase<CompositionConsumer> {
    public CreateCommentTask(CompositionConsumer consumer, Reply reply, String originalBody) {
        super(consumer, reply, null, originalBody);
    }

    @Override
    protected OID doMonitoredTask() {
        getNetworkContext().doCompositionTask(compositionConsumer.getCompositionPartition(), getCreateReplyTask());

        if (reply.isReplyLive()) {
            if (reply.isNew()) {
                compositionConsumer.getCompositionConsumerType().scheduleNewReplyEmailJob(compositionConsumer, reply);
            }

            SendNewMentionsNotificationEmailJob.schedule(null, compositionConsumer, reply.getOid());
        }

        return super.doMonitoredTask();
    }

}
