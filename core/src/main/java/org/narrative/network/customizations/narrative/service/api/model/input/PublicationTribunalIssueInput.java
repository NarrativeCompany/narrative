package org.narrative.network.customizations.narrative.service.api.model.input;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

/**
 * Date: 2019-08-02
 * Time: 11:49
 *
 * @author jonmark
 */
@Data
@Validated
@FieldNameConstants
public class PublicationTribunalIssueInput {
    @NotEmpty
    private final String comment;

    @Builder
    public PublicationTribunalIssueInput(@NotEmpty String comment) {
        this.comment = comment;
    }
}
