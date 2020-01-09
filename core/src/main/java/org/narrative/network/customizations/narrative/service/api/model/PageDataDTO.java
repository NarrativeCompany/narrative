package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.annotations.VisibleForTesting;
import org.narrative.network.customizations.narrative.serialization.jackson.JacksonConst;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.domain.Page;

import javax.validation.constraints.NotNull;

import java.util.List;

/**
 * Wrap {@link Page} data to allow custom JSON representation and also excludes redundant fields
 * during JSON serialization.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = JacksonConst.SER_TYPE_FIELD)
@JsonTypeName("PageData")
@JsonPropertyOrder({PageDataDTO.Fields.items,PageDataDTO.Fields.info})
@EqualsAndHashCode
@ToString
@FieldNameConstants
public class PageDataDTO<T> {
    private final List<T> items;
    private final PageInfo<T> info;

    /**
     * Constructor of {@code PageDataDTO}.
     *
     * @param page source {@link Page}
     */
    public PageDataDTO(@NotNull Page<T> page) {
        this.items = page.getContent();
        this.info = new PageInfo<>(page);
    }

    /**
     * For unit test de-serialization
     */
    @JsonCreator
    @VisibleForTesting
    PageDataDTO(@JsonProperty(PageDataDTO.Fields.items) List<T> items, @JsonProperty(PageDataDTO.Fields.info) PageDataDTO.PageInfo<T> info){
        this.items = items;
        this.info = info;
    }

    /**
     * Override getter so we can control serialization.
     */
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = JacksonConst.SER_TYPE_FIELD)
    public List<T> getItems() {
        return items;
    }

    public PageInfo getInfo() {
        return info;
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = JacksonConst.SER_TYPE_FIELD)
    @JsonTypeName("PageInfo")
    @Value
    @FieldNameConstants
    public static class PageInfo<T> {
        private final int totalPages;
        private final long totalElements;
        private final int number;
        private final int size;
        private final int numberOfElements;
        private final boolean first;
        private final boolean last;

        private PageInfo(Page<T> page) {
            this.totalPages = page.getTotalPages();
            this.totalElements = page.getTotalElements();
            this.number = page.getNumber();
            this.size = page.getSize();
            this.numberOfElements = page.getNumberOfElements();
            this.first = page.isFirst();
            this.last = page.isLast();
        }

        /**
         * For unit test de-serialization
         */
        @JsonCreator
        @VisibleForTesting
        PageInfo(@JsonProperty(Fields.totalPages) int totalPages,
                        @JsonProperty(Fields.totalElements) long totalElements,
                        @JsonProperty(Fields.number) int number,
                        @JsonProperty(Fields.size) int size,
                        @JsonProperty(Fields.numberOfElements) int numberOfElements,
                        @JsonProperty(Fields.first) boolean first,
                        @JsonProperty(Fields.last) boolean last) {
            this.totalPages = totalPages;
            this.totalElements = totalElements;
            this.number = number;
            this.size = size;
            this.numberOfElements = numberOfElements;
            this.first = first;
            this.last = last;
        }
    }
}
