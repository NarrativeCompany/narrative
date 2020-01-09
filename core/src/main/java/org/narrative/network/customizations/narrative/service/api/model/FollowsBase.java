package org.narrative.network.customizations.narrative.service.api.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldNameConstants;

import java.util.List;

/**
 * Date: 2019-03-22
 * Time: 20:03
 *
 * @author jonmark
 */
@Data
@FieldNameConstants
@EqualsAndHashCode(callSuper = true)
public class FollowsBase<T> extends LoadMoreItemsBase<T> {
    public final FollowScrollParamsDTO scrollParams;

    public FollowsBase(List<T> items, boolean hasMoreItems, FollowScrollParamsDTO scrollParams) {
        super(items, hasMoreItems);
        this.scrollParams = scrollParams;
    }
}
