package org.narrative.network.customizations.narrative.service.api.model.input;

import org.narrative.network.customizations.narrative.publications.PublicationPlanType;
import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

/**
 * Date: 2019-08-08
 * Time: 12:43
 *
 * @author jonmark
 */
@Data
@Validated
@FieldNameConstants
public class PublicationPlanInput {
    @NotNull
    private final PublicationPlanType plan;
}
