package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.common.persistence.OID;
import org.narrative.network.core.narrative.rewards.ContentCreatorRewardRole;
import org.narrative.network.core.narrative.wallet.WalletTransactionStatus;
import org.narrative.network.core.narrative.wallet.WalletTransactionType;
import org.narrative.network.customizations.narrative.NrveUsdValue;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

import java.time.Instant;

/**
 * Date: 11/28/18
 * Time: 7:23 AM
 *
 * @author brian
 */
@JsonValueObject
@JsonTypeName("UserRewardTransaction")
@Value
@Builder(toBuilder = true)
@FieldNameConstants
public class UserRewardTransactionDTO {
    private final OID oid;
    private final Instant transactionDatetime;
    private final WalletTransactionType type;
    private final WalletTransactionStatus status;
    private final UserDTO metadataUser;
    private final NicheDTO metadataNiche;
    private final PostDTO metadataPost;
    private final ContentCreatorRewardRole metadataContentCreatorRewardRole;
    private final Integer metadataActivityBonusPercentage;
    private final String metadataNeoWalletAddress;
    private final String metadataNeoTransactionId;
    private final String memo;
    private final NrveUsdValue amount;
}
