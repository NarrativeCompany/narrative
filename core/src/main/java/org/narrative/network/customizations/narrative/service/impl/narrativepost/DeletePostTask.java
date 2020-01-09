package org.narrative.network.customizations.narrative.service.impl.narrativepost;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.composition.base.services.DeleteCompositionConsumer;
import org.narrative.network.core.content.base.Content;
import org.narrative.network.customizations.narrative.controller.PostController;
import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntry;
import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntryType;
import org.narrative.network.customizations.narrative.niches.ledgerentries.services.SaveLedgerEntryTask;
import org.narrative.network.customizations.narrative.service.impl.tribunal.RecordUserActivityEventsForAupReportersTask;
import org.narrative.network.shared.security.AccessViolation;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import org.narrative.network.shared.tasktypes.CompositionTaskImpl;

import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: 2019-01-14
 * Time: 11:01
 *
 * @author jonmark
 */
public class DeletePostTask extends AreaTaskImpl<Object> {
    private final OID postOid;

    DeletePostTask(OID postOid) {
        this.postOid = postOid;
    }

    @Override
    protected Object doMonitoredTask() {
        getNetworkContext().getPrimaryRole().checkRegisteredUser();

        Content content = Content.dao().getForApiParam(postOid, PostController.POST_OID_PARAM);
        if (!content.isDeletableByCurrentUser() && !getNetworkContext().getPrimaryRole().isCanRemoveAupViolations()) {
            throw new AccessViolation(wordlet("deletePostTask.cannotDeleteThisPost"));
        }

        // bl: if the current user isn't the author, then it must be a delete by a Tribunal member.
        boolean isTribunalDeletion = !content.isCurrentUserAuthor();

        // bl: if this is a Tribunal deletion, then we need to record UserActivityRewardEvent records
        // for everyone who reported it as an AUP violation so they get activity points for doing so.
        if(isTribunalDeletion) {
            // jw: this must be ran within the context of the Composition partition for this consumer so that we can
            //     load the UserQualityRatedComposition object.
            getNetworkContext().doCompositionTask(content.getCompositionPartition(), new CompositionTaskImpl<Object>() {
                @Override
                protected Object doMonitoredTask() {
                    // bl: look up everyone who had reported the AUP violation before we go deleting all of the records :)
                    getNetworkContext().doGlobalTask(new RecordUserActivityEventsForAupReportersTask<>(content));
                    return null;
                }
            });
        }

        getAreaContext().doAreaTask(new DeleteCompositionConsumer(content));

        // finally, record a LedgerEntry
        {
            // bl: don't record a ledger entry when deleting drafts!
            boolean isDeleteLivePost = content.isContentLive();
            LedgerEntryType ledgerEntryType = isTribunalDeletion ? LedgerEntryType.TRIBUNAL_USER_DELETED_POST_OR_COMMENT_AUP_VIOLATION : isDeleteLivePost ? LedgerEntryType.USER_DELETED_POST : null;
            if(ledgerEntryType!=null) {
                LedgerEntry ledgerEntry = new LedgerEntry(getAreaContext().getAreaUserRlm(), ledgerEntryType);
                ledgerEntry.setContentOid(postOid);
                // bl: for Tribunal deletions, we also need to track the original post author on the ledger entry
                if(isTribunalDeletion) {
                    ledgerEntry.setAuthor(content.getUser());
                }
                getNetworkContext().doGlobalTask(new SaveLedgerEntryTask(ledgerEntry));
            }
        }

        // bl: if this is a Tribunal deletion, we also need to record the ledger entry for the author.
        // note that this will also create the Conduct Negative event for the user.
        if(isTribunalDeletion) {
            LedgerEntryType ledgerEntryType = LedgerEntryType.USER_HAD_POST_OR_COMMENT_DELETED_FOR_AUP_VIOLATION;
            LedgerEntry ledgerEntry = new LedgerEntry(content.getAreaUserRlm(), ledgerEntryType);
            ledgerEntry.setContentOid(postOid);
            getNetworkContext().doGlobalTask(new SaveLedgerEntryTask(ledgerEntry));
        }

        return null;
    }
}
