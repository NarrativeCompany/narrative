package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Date: 2019-04-05
 * Time: 08:49
 *
 * @author jonmark
 */
@JsonValueObject
@JsonTypeName("LedgerEntryScrollParams")
@Data
@NoArgsConstructor
public class LedgerEntryScrollParamsDTO {
    // bl: intentionally making these non-final so that they have accessors Spring needs in order to deserialize the values
    private Instant lastItemDatetime;

    @Builder(toBuilder = true)
    public LedgerEntryScrollParamsDTO(Instant lastItemDatetime) {
        this.lastItemDatetime = lastItemDatetime;
    }
}
