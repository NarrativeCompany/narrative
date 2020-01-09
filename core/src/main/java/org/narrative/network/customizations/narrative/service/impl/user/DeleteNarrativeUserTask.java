package org.narrative.network.customizations.narrative.service.impl.user;

import org.narrative.network.core.user.User;
import org.narrative.network.core.user.services.DeleteUserAccountContentTask;
import org.narrative.network.core.user.services.DeleteUserTask;
import org.narrative.network.core.user.services.SendUserDeletedOrActivationStatusChangeEmailTask;
import org.narrative.network.customizations.narrative.service.api.ValidationContext;
import org.narrative.network.customizations.narrative.service.api.model.input.DeleteUserInput;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 9/28/18
 * Time: 4:17 PM
 *
 * @author brian
 */
public class DeleteNarrativeUserTask extends UpdateProfileAccountConfirmationBaseTask<Object> {

    public DeleteNarrativeUserTask(User user, DeleteUserInput deleteUser) {
        super(user, deleteUser);
    }

    @Override
    protected void validate(ValidationContext validationContext) {
        super.validate(validationContext);

        if (!isEmptyOrNull(getAreaContext().getAreaUserRlm().getLeadingBidderForAuctions())) {
            validationContext.addMethodError("DeleteNarrativeUserTask.cantDeleteAccount.leadingBidder");
        }
        if (!isEmptyOrNull(getAreaContext().getAreaUserRlm().getOutstandingInvoiceLookup())) {
            validationContext.addMethodError("DeleteNarrativeUserTask.cantDeleteAccount.unpaidInvoice");
        }
    }

    @Override
    protected Object doMonitoredTask() {
        //mk: need original email and name as they are replaced after deletion
        String originalEmail = getUser().getEmailAddress();
        String originalName = getUser().getDisplayName();

        getAreaContext().doAreaTask(new DeleteUserTask(getUser().getOid()));

        // bl: delete all of the user's content synchronously, too.
        getNetworkContext().doGlobalTask(new DeleteUserAccountContentTask(getUser(), originalName));

        // jw: send an email to the user so they have a record of this change. Important if someone else got access to their account and did this for them.
        getAreaContext().doAreaTask(new SendUserDeletedOrActivationStatusChangeEmailTask(getUser(), originalName, originalEmail, true));

        return null;
    }
}
