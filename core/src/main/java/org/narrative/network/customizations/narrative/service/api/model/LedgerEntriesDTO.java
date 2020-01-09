package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

import java.util.List;

/**
 * Date: 10/4/18
 * Time: 9:52 AM
 *
 * @author jonmark
 */
@JsonValueObject
@JsonTypeName("LedgerEntries")
@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class LedgerEntriesDTO extends LoadMoreItemsBase<LedgerEntryDTO> {
    private final LedgerEntryScrollParamsDTO scrollParams;

    @Builder(toBuilder = true)
    public LedgerEntriesDTO(List<LedgerEntryDTO> items, boolean hasMoreItems, LedgerEntryScrollParamsDTO scrollParams) {
        super(items, hasMoreItems);
        this.scrollParams = scrollParams;
    }
}
