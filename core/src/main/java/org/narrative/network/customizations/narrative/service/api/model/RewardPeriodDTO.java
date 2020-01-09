package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

import java.time.YearMonth;

/**
 * Date: 2019-06-03
 * Time: 15:53
 *
 * @author brian
 */
@JsonValueObject
@JsonTypeName("RewardPeriod")
@Value
@Builder(toBuilder = true)
@FieldNameConstants
public class RewardPeriodDTO {
    private final String name;
    private final YearMonth yearMonth;
}
