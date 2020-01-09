package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.domain.Page;

import javax.validation.constraints.NotNull;

@JsonValueObject
@JsonTypeName("Comments")
@Value
@FieldNameConstants
@EqualsAndHashCode(callSuper = true)
public class CommentsDTO extends PageDataDTO<CommentDTO> {
    private final Integer buriedCommentCount;
    private final Boolean includeBuried;

    @Builder
    public CommentsDTO(@NotNull Page<CommentDTO> page, Integer buriedCommentCount, Boolean includeBuried) {
        super(page);
        this.buriedCommentCount = buriedCommentCount;
        this.includeBuried = includeBuried;
    }
}
