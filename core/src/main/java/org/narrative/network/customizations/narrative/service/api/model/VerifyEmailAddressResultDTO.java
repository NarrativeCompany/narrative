package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.network.core.user.EmailAddressVerificationStep;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

import java.util.Set;

/**
 * Date: 2019-07-11
 * Time: 14:45
 *
 * @author jonmark
 */
@JsonValueObject
@JsonTypeName("VerifyEmailAddressResult")
@Value
@Builder(toBuilder = true)
@FieldNameConstants
public class VerifyEmailAddressResultDTO {
    // jw: once verification is complete we need to show the user their new email address.
    private final String emailAddress;
    // jw: this represents the email address left to verify. It can be null if the viewer is a guest and the email address
    //     that needs verification is the PRIMARY email address.
    private final String emailAddressToVerify;
    // jw: this represents the verification steps the users email still requires.
    private final Set<EmailAddressVerificationStep> incompleteVerificationSteps;
    // jw: this will be provided if the email address is fully verified and the user is logged in when doing the verification.
    private final TokenDTO token;
}
