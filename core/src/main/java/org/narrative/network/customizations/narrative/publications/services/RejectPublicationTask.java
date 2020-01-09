package org.narrative.network.customizations.narrative.publications.services;

import org.narrative.network.core.cluster.partition.PartitionGroup;
import org.narrative.network.customizations.narrative.channels.Channel;
import org.narrative.network.customizations.narrative.niches.tribunal.TribunalIssue;
import org.narrative.network.customizations.narrative.publications.Publication;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-08-02
 * Time: 10:05
 *
 * @author jonmark
 */
public class RejectPublicationTask extends AreaTaskImpl<Object> {
    private final Publication publication;
    private final TribunalIssue dueToIssue;

    public RejectPublicationTask(Publication publication, TribunalIssue dueToIssue) {
        this.publication = publication;
        this.dueToIssue = dueToIssue;
    }

    @Override
    protected Object doMonitoredTask() {
        Channel channel = publication.getChannel();

        // jw: we need to give a pro-rated refund for the purchase invoice if one exists.
        if (exists(channel.getPurchaseInvoice())) {
            channel.refundPurchaseInvoice();
        }

        // jw: send an email to the owner to let the me know that the publication has been rejected.
        getAreaContext().doAreaTask(new SendPublicationRejectedEmailToOwner(publication, dueToIssue));

        // jw: we need to flush hibernate to ensure that anything that had used or referenced this publication to this
        //     point is flushed. That way the publication deletion process can modify everything, and clean things up.
        PartitionGroup.getCurrentPartitionGroup().flushAllSessions();

        // jw: at this point all we have left to do is delete the publication... Scary stuff.
        getAreaContext().doAreaTask(new DeletePublicationTask(publication, true));

        return null;
    }
}
