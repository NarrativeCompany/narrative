package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

/**
 * Date: 2019-09-23
 * Time: 1:18
 *
 * @author brian
 */
@JsonValueObject
@JsonTypeName("PublicationDiscount")
@Value
@Builder(toBuilder = true)
@FieldNameConstants
public class PublicationDiscountDTO {
    private final OID oid;
    private final boolean eligibleForDiscount;
}
