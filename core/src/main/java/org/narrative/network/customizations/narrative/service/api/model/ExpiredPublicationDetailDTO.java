package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

/**
 * Date: 2019-09-26
 * Time: 14:17
 *
 * @author jonmark
 */
@JsonValueObject
@JsonTypeName("ExpiredPublicationDetail")
@Value
@Builder
public class ExpiredPublicationDetailDTO {
    // jw: need these fields to render the proper error to the user.
    private final PublicationDTO publication;
    private final Instant deletionDatetime;
    private final boolean owner;
}
