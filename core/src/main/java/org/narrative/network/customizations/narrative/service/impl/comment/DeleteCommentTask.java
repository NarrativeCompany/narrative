package org.narrative.network.customizations.narrative.service.impl.comment;

import org.narrative.network.core.area.user.AreaUser;
import org.narrative.network.core.composition.base.CompositionConsumer;
import org.narrative.network.core.composition.base.Reply;
import org.narrative.network.core.content.base.Content;
import org.narrative.network.core.content.base.services.DeleteReplyFully;
import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntry;
import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntryType;
import org.narrative.network.customizations.narrative.niches.ledgerentries.services.SaveLedgerEntryTask;
import org.narrative.network.customizations.narrative.posts.services.SendCommentRemovedFromPublicationPostEmailTask;
import org.narrative.network.customizations.narrative.publications.Publication;
import org.narrative.network.customizations.narrative.service.impl.tribunal.RecordUserActivityEventsForAupReportersTask;
import org.narrative.network.shared.tasktypes.CompositionTaskImpl;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: 2019-02-14
 * Time: 11:04
 *
 * @author jonmark
 */
public class DeleteCommentTask extends CommentTaskBase<Object> {
    public DeleteCommentTask(CompositionConsumer consumer, Reply reply) {
        super(consumer, reply);
    }

    @Override
    protected Object doMonitoredTask() {
        // bl: if the current user is not the author, then it's a Tribunal deletion for AUP violation
        // bl: important! if the post is in a publication, the publication editors deleting should _not_ be considered
        // a delete by tribunal for an AUP violation! this is evidenced by the fact that the user can delete all replies
        boolean isTribunalDeletion = !reply.isCurrentUserAuthor() && !consumer.isRepliesDeletableByCurrentUser();
        boolean isPublicationEditorDeletion = !reply.isCurrentUserAuthor() && consumer.isRepliesDeletableByCurrentUser();

        // bl: if this is a Tribunal deletion, then we need to record UserActivityRewardEvent records
        // for everyone who reported it as an AUP violation so they get activity points for doing so.
        if(isTribunalDeletion) {
            // jw: this must be ran within the context of the Composition partition for this consumer so that we can
            //     load the UserQualityRatedReply object.
            getNetworkContext().doCompositionTask(consumer.getCompositionPartition(), new CompositionTaskImpl<Object>() {
                @Override
                protected Object doMonitoredTask() {
                    // bl: look up everyone who had reported the AUP violation before we go deleting all of the records :)
                    getNetworkContext().doGlobalTask(new RecordUserActivityEventsForAupReportersTask<>(reply));
                    return null;
                }
            });
        }

        // jw: first, let's delete the reply using our standard method.
        networkContext().doCompositionTask(consumer.getCompositionPartition(), new DeleteReplyFully(reply, consumer));

        // jw: now, let's see if we have any type specific work to do.
        CommentTaskBase deletionHandler = consumer.getCompositionConsumerType().getDeleteCommentHandlerTask(consumer, reply);

        // jw: the handler is not required, so only run it if we have one.
        if (deletionHandler!=null) {
            getAreaContext().doAreaTask(deletionHandler);
        }

        if(isTribunalDeletion) {
            {
                LedgerEntry ledgerEntry = new LedgerEntry(getAreaContext().getAreaUserRlm(), LedgerEntryType.TRIBUNAL_USER_DELETED_POST_OR_COMMENT_AUP_VIOLATION);
                ledgerEntry.setCommentOid(reply.getOid());
                // bl: for Tribunal deletions, we also need to track the original post author on the ledger entry
                ledgerEntry.setAuthor(reply.getUser());
                getNetworkContext().doGlobalTask(new SaveLedgerEntryTask(ledgerEntry));
            }

            // bl: we also need to record the ledger entry for the author.
            // note that this will also create the Conduct Negative event for the user.
            {
                LedgerEntry ledgerEntry = new LedgerEntry(AreaUser.getAreaUserRlm(reply.getAuthor().getLoneAreaUser()), LedgerEntryType.USER_HAD_POST_OR_COMMENT_DELETED_FOR_AUP_VIOLATION);
                ledgerEntry.setCommentOid(reply.getOid());
                getNetworkContext().doGlobalTask(new SaveLedgerEntryTask(ledgerEntry));
            }
        } else if(isPublicationEditorDeletion) {
            assert consumer.getCompositionType().isContent() : "Should only find posts deleted by Publication Editors that are content, not/" + consumer.getCompositionType();
            Content content = cast(consumer, Content.class);
            Publication publication = content.getPublishedToPublication();
            {
                LedgerEntry ledgerEntry = new LedgerEntry(getAreaContext().getAreaUserRlm(), LedgerEntryType.PUBLICATION_EDITOR_DELETED_COMMENT);
                ledgerEntry.setCommentOid(reply.getOid());
                // bl: for comment deletions, we also need to track the original post author on the ledger entry
                ledgerEntry.setAuthor(reply.getUser());
                ledgerEntry.setChannel(publication.getChannel());
                ledgerEntry.setContentOid(content.getOid());
                getNetworkContext().doGlobalTask(new SaveLedgerEntryTask(ledgerEntry));
            }

            // bl: we also need to record the ledger entry for the author.
            {
                LedgerEntry ledgerEntry = new LedgerEntry(AreaUser.getAreaUserRlm(reply.getAuthor().getLoneAreaUser()), LedgerEntryType.USER_HAD_COMMENT_DELETED_BY_PUBLICATION_EDITOR);
                ledgerEntry.setCommentOid(reply.getOid());
                ledgerEntry.setChannel(publication.getChannel());
                ledgerEntry.setContentOid(content.getOid());
                getNetworkContext().doGlobalTask(new SaveLedgerEntryTask(ledgerEntry));
            }

            // bl: send the comment author an email to let them know the post was removed
            getAreaContext().doAreaTask(new SendCommentRemovedFromPublicationPostEmailTask(content, publication, getNetworkContext().getUser()));
        }

        return null;
    }
}
