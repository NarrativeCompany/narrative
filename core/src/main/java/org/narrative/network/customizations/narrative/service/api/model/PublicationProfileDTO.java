package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.publications.PublicationContentRewardRecipientType;
import org.narrative.network.customizations.narrative.publications.PublicationContentRewardWriterShare;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

import java.time.Instant;
import java.util.List;

/**
 * Date: 2019-08-01
 * Time: 15:26
 *
 * @author jonmark
 */
@JsonValueObject
@JsonTypeName("PublicationProfile")
@Value
@Builder(toBuilder = true)
@FieldNameConstants
public class PublicationProfileDTO {
    private final OID oid;

    private final Instant creationDatetime;
    private final int followerCount;
    private final boolean canCurrentUserAppeal;

    private final List<UserDTO> admins;
    private final List<UserDTO> editors;
    private final List<UserDTO> writers;

    private final PublicationContentRewardWriterShare contentRewardWriterShare;
    private final PublicationContentRewardRecipientType contentRewardRecipient;
}
