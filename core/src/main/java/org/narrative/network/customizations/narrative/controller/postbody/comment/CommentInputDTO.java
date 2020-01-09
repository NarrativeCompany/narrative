package org.narrative.network.customizations.narrative.controller.postbody.comment;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.narrative.network.customizations.narrative.service.api.model.input.CommentInput;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

/**
 * Date: 10/27/18
 * Time: 10:21 AM
 *
 * @author brian
 */
@Validated
public class CommentInputDTO extends CommentInput {
    @JsonCreator
    public CommentInputDTO(@NotEmpty String body) {
        super(body);
    }
}
