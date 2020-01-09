package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

import java.util.List;

@JsonValueObject
@JsonTypeName("SearchResults")
@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SearchResultsDTO extends LoadMoreItemsBase<SearchResultDTO> {
    @Builder(toBuilder = true)
    public SearchResultsDTO(List<SearchResultDTO> items, boolean hasMoreItems, Integer lastResultIndex) {
        super(items, hasMoreItems);
        this.lastResultIndex = lastResultIndex;
    }

    private Integer lastResultIndex;
}
