package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.narrative.network.customizations.narrative.serialization.jackson.JacksonConst;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

import java.util.List;

/**
 * Date: 10/4/18
 * Time: 10:09 AM
 *
 * jw: this is meant to be a base for XItemsDTO objects, like LedgerEntriesDTO, or SearchResultsDTO
 *
 * @author jonmark
 */
@Data
@FieldNameConstants
public class LoadMoreItemsBase<T> {
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = JacksonConst.SER_TYPE_FIELD)
    public final List<T> items;

    public final boolean hasMoreItems;
}
