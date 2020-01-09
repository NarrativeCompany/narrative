package org.narrative.network.customizations.narrative.service.api.model.input;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

/**
 * Date: 2019-02-15
 * Time: 10:00
 *
 * @author jonmark
 */
@Data
@EqualsAndHashCode()
@ToString(callSuper = true)
@Validated
@FieldNameConstants
public class CommentInput {
    @NotEmpty
    private final String body;

    @Builder
    public CommentInput(@NotEmpty String body) {
        this.body = body;
    }
}
