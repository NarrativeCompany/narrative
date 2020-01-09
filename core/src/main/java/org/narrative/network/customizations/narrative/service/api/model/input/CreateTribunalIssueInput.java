package org.narrative.network.customizations.narrative.service.api.model.input;

import org.narrative.network.customizations.narrative.niches.tribunal.TribunalIssueType;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

/**
 * Date: 10/11/18
 * Time: 7:13 AM
 *
 * @author brian
 */
@Data
@Validated
@FieldNameConstants
public class CreateTribunalIssueInput {
    @NotNull
    private final TribunalIssueType type;

    /**
     * comment is optional for niche edits, so not annotating with @NotEmpty
     */
    private final String comment;

    @Builder
    public CreateTribunalIssueInput(@NotNull TribunalIssueType type, String comment) {
        this.type = type;
        this.comment = comment;
    }
}
