package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.common.persistence.OID;
import org.narrative.network.core.narrative.wallet.NeoWalletType;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

/**
 * Date: 10/1/19
 * Time: 1:14 PM
 *
 * @author brian
 */
@JsonValueObject
@JsonTypeName("NeoWallet")
@Value
@Builder
@FieldNameConstants
public class NeoWalletDTO {
    private final OID oid;
    private final NeoWalletType type;
    private final String neoAddress;
    private final String extraNeoAddress;
    private final String scriptHash;
    private final String monthForDisplay;
}
