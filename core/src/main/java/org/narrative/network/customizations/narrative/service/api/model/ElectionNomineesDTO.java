package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

import java.time.Instant;
import java.util.List;

/**
 * Date: 11/15/18
 * Time: 1:03 PM
 *
 * @author jonmark
 */
@JsonValueObject
@JsonTypeName("ElectionNominees")
@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ElectionNomineesDTO extends LoadMoreItemsBase<ElectionNomineeDTO> {
    private final Instant lastItemConfirmationDatetime;

    // jw: this is unfortunately necessary for ElectionControllerSpec testing
    public ElectionNomineesDTO() {
        super(null, false);
        this.lastItemConfirmationDatetime = null;
    }

    @Builder(toBuilder = true)
    public ElectionNomineesDTO(List<ElectionNomineeDTO> items, boolean hasMoreItems, Instant lastItemConfirmationDatetime) {
        super(items, hasMoreItems);
        this.lastItemConfirmationDatetime = lastItemConfirmationDatetime;
    }
}
