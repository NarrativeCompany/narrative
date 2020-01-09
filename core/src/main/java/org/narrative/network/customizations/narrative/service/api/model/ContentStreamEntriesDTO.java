package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

import java.util.List;

/**
 * Date: 2019-02-27
 * Time: 12:35
 *
 * @author brian
 */
@JsonValueObject
@JsonTypeName("CommentStreamEntries")
@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ContentStreamEntriesDTO extends LoadMoreItemsBase<PostDTO> {
    private final ContentStreamScrollParamsDTO scrollParams;

    @Builder(toBuilder = true)
    public ContentStreamEntriesDTO(List<PostDTO> items, boolean hasMoreItems, ContentStreamScrollParamsDTO scrollParams) {
        super(items, hasMoreItems);
        this.scrollParams = scrollParams;
    }
}
