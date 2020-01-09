package org.narrative.network.customizations.narrative.controller.postbody.referendum;

import org.narrative.network.customizations.narrative.niches.referendum.ReferendumVoteReason;
import lombok.Builder;
import lombok.Value;
import org.springframework.validation.annotation.Validated;
import javax.validation.constraints.NotNull;

@Value
@Builder
@Validated
public class ReferendumVoteInputDTO {
    @NotNull
    private final Boolean votedFor;
    private final ReferendumVoteReason reason;
    private final String comment;
}
