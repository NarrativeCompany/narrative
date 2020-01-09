package org.narrative.network.customizations.narrative.util;

import org.narrative.network.customizations.narrative.service.api.model.FollowScrollParamsDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.validation.annotation.Validated;

/**
 * Date: 2019-03-22
 * Time: 20:39
 *
 * @author jonmark
 */
@Data
@Validated
@EqualsAndHashCode(callSuper = true)
public class FollowedItemScrollable extends Scrollable {
    // bl: this can't be final in order for Spring's nested property handling to work. has to be exposed through a setter.
    private FollowScrollParamsDTO scrollParams;

    public FollowedItemScrollable(Integer count, FollowScrollParamsDTO scrollParams) {
        super(count);
        this.scrollParams = scrollParams;
    }
}
