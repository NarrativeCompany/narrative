package org.narrative.network.customizations.narrative.controller.postbody.tribunal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.narrative.network.customizations.narrative.niches.tribunal.TribunalIssueType;
import org.narrative.network.customizations.narrative.service.api.model.input.CreateTribunalIssueInput;

/**
 * Date: 10/11/18
 * Time: 7:16 AM
 *
 * @author brian
 */
public class CreateTribunalIssueInputDTO extends CreateTribunalIssueInput {
    @JsonCreator
    public CreateTribunalIssueInputDTO(@JsonProperty(Fields.type) TribunalIssueType type,
                                       @JsonProperty(Fields.comment) String comment) {
        super(type, comment);
    }
}
