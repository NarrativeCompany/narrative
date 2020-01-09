package org.narrative.network.customizations.narrative.util;

import org.narrative.config.properties.NarrativeProperties;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Positive;

/**
 * Date: 2019-02-27
 * Time: 11:46
 *
 * @author brian
 */
@Data
@Validated
public class Scrollable {
    /**
     * the count of items per page. don't allow direct access to the count since we want all checks to go
     * through getResolvedCount so we can enforce max page sizes.
     */
    @Positive
    @Getter(AccessLevel.NONE)
    private final int count;

    public Scrollable(Integer count) {
        this.count = count==null ? 25 : count;
    }

    public int getResolvedCount(NarrativeProperties narrativeProperties) {
        return Math.min(count, narrativeProperties.getSpring().getMvc().getMaxPageSize());
    }
}
