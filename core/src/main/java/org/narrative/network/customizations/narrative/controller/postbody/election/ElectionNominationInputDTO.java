package org.narrative.network.customizations.narrative.controller.postbody.election;

import org.narrative.network.customizations.narrative.elections.ElectionNominee;
import lombok.Builder;
import lombok.Value;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Size;

/**
 * Date: 11/19/18
 * Time: 9:01 AM
 *
 * @author jonmark
 */
@Value
@Builder
@Validated
public class ElectionNominationInputDTO {
    @Size(max = ElectionNominee.MAX_PERSONAL_STATEMENT_SIZE)
    private final String personalStatement;
}
