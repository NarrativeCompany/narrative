package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.common.persistence.OID;
import org.narrative.network.core.user.UserRedemptionStatus;
import org.narrative.network.customizations.narrative.NrveValueDetail;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

import java.time.Instant;

/**
 * Date: 2019-06-27
 * Time: 09:11
 *
 * @author jonmark
 */
@JsonValueObject
@JsonTypeName("UserNeoWallet")
@Value
@Builder(toBuilder = true)
@FieldNameConstants
public class UserNeoWalletDTO {
    public OID oid;
    public String neoAddress;
    public UserRedemptionStatus redemptionStatus;
    // jw: this should only be set if the User's wallet is still within the waiting period.
    public Instant waitingPeriodEndDatetime;
    public NrveValueDetail currentBalance;
}
