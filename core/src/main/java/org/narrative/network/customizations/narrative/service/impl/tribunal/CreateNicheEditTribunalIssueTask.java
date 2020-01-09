package org.narrative.network.customizations.narrative.service.impl.tribunal;

import org.narrative.common.util.ApplicationError;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntryType;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.customizations.narrative.niches.referendum.Referendum;
import org.narrative.network.customizations.narrative.niches.referendum.ReferendumType;
import org.narrative.network.customizations.narrative.niches.referendum.metadata.NicheDetailChangeReferendumMetadata;
import org.narrative.network.customizations.narrative.niches.tribunal.TribunalIssueType;
import org.narrative.network.customizations.narrative.niches.tribunal.services.CreateTribunalIssueTask;
import org.narrative.network.customizations.narrative.service.api.ValidationContext;
import org.narrative.network.customizations.narrative.service.api.model.input.NicheInputBase;

import java.util.Collections;
import java.util.List;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: 10/11/18
 * Time: 7:56 AM
 *
 * @author brian
 */
public class CreateNicheEditTribunalIssueTask extends CreateTribunalIssueBaseTask<Niche> {
    private final NicheInputBase nicheInput;

    public CreateNicheEditTribunalIssueTask(Niche niche, NicheInputBase nicheInput) {
        super(niche, TribunalIssueType.APPROVE_NICHE_DETAIL_CHANGE, null);
        this.nicheInput = nicheInput;
    }

    @Override
    LedgerEntryType getLedgerEntryType() {
        return LedgerEntryType.NICHE_EDIT;
    }

    @Override
    CreateTribunalIssueTask getCreateTribunalIssueTask() {
        return new CreateTribunalIssueTask(channelConsumer, nicheInput.getName(), nicheInput.getDescription());
    }

    @Override
    protected void validate(ValidationContext validationContext) {
        super.validate(validationContext);

        /*
         * From SubmitNicheDetailsActionBase#checkRightAfterParams
         */
        boolean hasNiche = exists(channelConsumer);
        if (!hasNiche) {
            throw UnexpectedError.getRuntimeException("A valid Niche must be provided for an update operation");
        }

        if (!channelConsumer.isCanCurrentRoleEditDetails()) {
            throw new ApplicationError(wordlet("submitNicheDetailsActionBase.changeNotAllowed"));
        }

        if (channelConsumer.isHasOpenDetailChangeReferendums()) {
            throw new ApplicationError(wordlet("submitNicheDetailsActionBase.referendumAlreadyOpen"));
        }

        /*
         * From SubmitNicheDetailsActionBase#validate
         */
        Niche duplicateNiche = Niche.dao().getForReservedName(getAreaContext().getPortfolio(), nicheInput.getName());
        if (exists(duplicateNiche) && !isEqual(channelConsumer, duplicateNiche)) {
            validationContext.addFieldError(NicheInputBase.Fields.name, "submitNicheDetailsActionBase.nameIsAlreadyInUse");
        } else {  // jw: we only want to do addition niche name validation if we did not find any other issues with the name!
            //mk: make sure there are no other details change referendums open with same suggested name
            List<Referendum> referendumList = Referendum.dao().getReferendumsByTypeAndStatus(Collections.singletonList(ReferendumType.TRIBUNAL_APPROVE_NICHE_DETAIL_CHANGE), true, 1, Integer.MAX_VALUE);
            for (Referendum referendum : referendumList) {
                assert referendum.getType().isTribunalApproveNicheDetails() : "Type should be ReferendumType.TRIBUNAL_APPROVE_NICHE_DETAIL_CHANGE and not /" + referendum.getType();

                NicheDetailChangeReferendumMetadata metadata = referendum.getMetadata();
                if (metadata.isWasNameChanged() && isEqual(metadata.getNewName(), nicheInput.getName())) {
                    validationContext.addFieldError(NicheInputBase.Fields.name, "submitNicheDetailsActionBase.nameIsAlreadyInUse");
                }
            }
        }

        if (exists(channelConsumer) && isEqual(channelConsumer.getName(), nicheInput.getName()) && isEqual(channelConsumer.getDescription(), nicheInput.getDescription())) {
            validationContext.addMethodError("validateUpdateNicheRequest", "submitNicheDetailsActionBase.detailsMustBeChanged");
        }
    }
}
