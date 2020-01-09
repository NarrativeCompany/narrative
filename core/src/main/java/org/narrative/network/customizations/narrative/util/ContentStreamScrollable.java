package org.narrative.network.customizations.narrative.util;

import org.narrative.network.customizations.narrative.service.api.model.ContentStreamScrollParamsDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.validation.annotation.Validated;

/**
 * Date: 2019-03-09
 * Time: 12:56
 *
 * @author brian
 */
@Data
@Validated
@EqualsAndHashCode(callSuper = true)
public class ContentStreamScrollable extends Scrollable {
    // bl: this can't be final in order for Spring's nested property handling to work. has to be exposed through a setter.
    private ContentStreamScrollParamsDTO scrollParams;

    public ContentStreamScrollable(Integer count, ContentStreamScrollParamsDTO scrollParams) {
        super(count);
        this.scrollParams = scrollParams;
    }
}
