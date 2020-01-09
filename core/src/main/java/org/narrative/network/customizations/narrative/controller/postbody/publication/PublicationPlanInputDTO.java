package org.narrative.network.customizations.narrative.controller.postbody.publication;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.narrative.network.customizations.narrative.publications.PublicationPlanType;
import org.narrative.network.customizations.narrative.service.api.model.input.PublicationPlanInput;

/**
 * Date: 2019-08-08
 * Time: 12:44
 *
 * @author jonmark
 */
public class PublicationPlanInputDTO extends PublicationPlanInput {
    public PublicationPlanInputDTO(@JsonProperty(Fields.plan) PublicationPlanType plan) {
        super(plan);
    }
}
