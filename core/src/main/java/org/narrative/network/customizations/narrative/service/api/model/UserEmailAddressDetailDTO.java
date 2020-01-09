package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.common.persistence.OID;
import org.narrative.network.core.user.EmailAddressVerificationStep;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

import java.time.Instant;
import java.util.Collection;

/**
 * Date: 2019-07-12
 * Time: 08:51
 *
 * @author jonmark
 */
@JsonValueObject
@JsonTypeName("UserEmailAddressDetail")
@Value
@Builder(toBuilder = true)
@FieldNameConstants
public class UserEmailAddressDetailDTO {
    private final OID oid;
    private final String emailAddress;
    private final String pendingEmailAddress;
    private final Instant pendingEmailAddressExpirationDatetime;
    private final Collection<EmailAddressVerificationStep> incompleteVerificationSteps;
}
