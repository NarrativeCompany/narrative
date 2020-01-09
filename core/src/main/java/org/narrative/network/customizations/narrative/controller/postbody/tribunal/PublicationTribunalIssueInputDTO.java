package org.narrative.network.customizations.narrative.controller.postbody.tribunal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.narrative.network.customizations.narrative.service.api.model.input.PublicationTribunalIssueInput;

import javax.validation.constraints.NotEmpty;

/**
 * Date: 2019-08-02
 * Time: 11:51
 *
 * @author jonmark
 */
public class PublicationTribunalIssueInputDTO extends PublicationTribunalIssueInput {
    @JsonCreator
    public PublicationTribunalIssueInputDTO(@NotEmpty @JsonProperty(Fields.comment) String comment) {
        super(comment);
    }
}
