package org.narrative.network.customizations.narrative.service.impl.tribunal;

import org.narrative.network.customizations.narrative.channels.ChannelConsumer;
import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntryType;
import org.narrative.network.customizations.narrative.niches.tribunal.TribunalIssueType;
import org.narrative.network.customizations.narrative.niches.tribunal.services.CreateTribunalIssueTask;
import org.narrative.network.customizations.narrative.service.api.ValidationContext;
import org.narrative.network.customizations.narrative.service.api.model.input.CreateTribunalIssueInput;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 10/11/18
 * Time: 7:29 AM
 *
 * @author brian
 */
public class CreateChannelConsumerTribunalIssueTask extends CreateTribunalIssueBaseTask<ChannelConsumer> {

    CreateChannelConsumerTribunalIssueTask(ChannelConsumer channelConsumer, TribunalIssueType type, String comment) {
        super(channelConsumer, type, comment);
    }

    @Override
    LedgerEntryType getLedgerEntryType() {
        return LedgerEntryType.ISSUE_REPORT;
    }

    @Override
    CreateTribunalIssueTask getCreateTribunalIssueTask() {
        return new CreateTribunalIssueTask(channelConsumer, type);
    }

    @Override
    protected void validate(ValidationContext validationContext) {
        getAreaContext().getPrimaryRole().checkCanPostIssueToTribunal();

        if (!isEqual(channelConsumer.getPossibleTribunalIssueType(), type)) {
            validationContext.addInvalidFieldError(CreateTribunalIssueInput.Fields.type);
        }

        if(isEmpty(comment)) {
            validationContext.addRequiredFieldError(CreateTribunalIssueInput.Fields.comment);
        }
    }
}
