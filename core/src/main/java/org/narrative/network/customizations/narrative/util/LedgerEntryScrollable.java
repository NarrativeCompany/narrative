package org.narrative.network.customizations.narrative.util;

import org.narrative.network.customizations.narrative.service.api.model.LedgerEntryScrollParamsDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.validation.annotation.Validated;

/**
 * Date: 2019-04-05
 * Time: 08:51
 *
 * @author jonmark
 */
@Data
@Validated
@EqualsAndHashCode(callSuper = true)
public class LedgerEntryScrollable  extends Scrollable {
    // bl: this can't be final in order for Spring's nested property handling to work. has to be exposed through a setter.
    private LedgerEntryScrollParamsDTO scrollParams;

    public LedgerEntryScrollable(Integer count, LedgerEntryScrollParamsDTO scrollParams) {
        super(count);
        this.scrollParams = scrollParams;
    }
}
