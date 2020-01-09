package org.narrative.network.customizations.narrative.service.impl.niche;

import org.narrative.network.customizations.narrative.controller.postbody.niche.CreateNicheInputDTO;
import org.narrative.network.customizations.narrative.permissions.NarrativePermissionType;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.customizations.narrative.niches.referendum.Referendum;
import org.narrative.network.customizations.narrative.niches.referendum.ReferendumType;
import org.narrative.network.customizations.narrative.niches.referendum.metadata.NicheDetailChangeReferendumMetadata;
import org.narrative.network.customizations.narrative.service.api.ValidationContext;
import org.narrative.network.customizations.narrative.service.api.model.input.NicheInputBase;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

import java.util.Collections;
import java.util.List;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 10/18/18
 * Time: 7:49 AM
 *
 * @author brian
 */
public abstract class SubmitNicheDetailsBaseTask<T,NI extends NicheInputBase> extends AreaTaskImpl<T> {
    final NI nicheInput;

    SubmitNicheDetailsBaseTask(NI nicheInput) {
        this.nicheInput = nicheInput;
    }

    @Override
    protected void validate(ValidationContext validationContext) {
        /*
         * From SubmitNicheDetailsActionBase#checkRightAfterParams
         */
        NarrativePermissionType.SUGGEST_NICHES.checkRight(getAreaContext().getAreaRole());

        /*
         * From SubmitNicheDetailsActionBase#validate
         */
        Niche duplicateNiche = Niche.dao().getForReservedName(getAreaContext().getPortfolio(), nicheInput.getName());
        if (exists(duplicateNiche)) {
            validationContext.addFieldError(CreateNicheInputDTO.Fields.name, "submitNicheDetailsActionBase.nameIsAlreadyInUse");
        } else {
            // jw: we only want to do addition niche name validation if we did not find any other issues with the name!
            //mk: make sure there are no other details change referendums open with same suggested name
            List<Referendum> referendumList = Referendum.dao().getReferendumsByTypeAndStatus(Collections.singletonList(ReferendumType.TRIBUNAL_APPROVE_NICHE_DETAIL_CHANGE), true, 1, Integer.MAX_VALUE);
            for (Referendum referendum : referendumList) {
                assert referendum.getType().isTribunalApproveNicheDetails() : "Type should be ReferendumType.TRIBUNAL_APPROVE_NICHE_DETAIL_CHANGE and not /" + referendum.getType();

                NicheDetailChangeReferendumMetadata metadata = referendum.getMetadata();
                if (metadata.isWasNameChanged() && isEqual(metadata.getNewName(), nicheInput.getName())) {
                    validationContext.addFieldError(CreateNicheInputDTO.Fields.name, "submitNicheDetailsActionBase.nameIsAlreadyInUse");
                }
            }
        }
    }
}
