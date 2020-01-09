package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.publications.PublicationContentRewardRecipientType;
import org.narrative.network.customizations.narrative.publications.PublicationContentRewardWriterShare;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

/**
 * Date: 2019-08-23
 * Time: 14:27
 *
 * @author jonmark
 */
@JsonValueObject
@JsonTypeName("PublicationSettings")
@Value
@FieldNameConstants
@Builder
public class PublicationSettingsDTO {
    private final OID oid;

    // jw: this will provide many of the details necessary to drive the form, but more importantly we need to make sure
    //     we provide it so that once the form is submitted we will get a fresh object into cache and reflect any changes
    //     onto the UI.
    private final PublicationDetailDTO publicationDetail;

    private final PublicationContentRewardWriterShare contentRewardWriterShare;
    private final PublicationContentRewardRecipientType contentRewardRecipient;
}
