package org.narrative.network.customizations.narrative.service.mapper;

import org.narrative.network.core.user.EmailAddress;
import org.narrative.network.core.user.EmailAddressVerificationStep;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.service.api.model.VerifyEmailAddressResultDTO;
import org.narrative.network.customizations.narrative.service.impl.user.UpdateProfileAndJwtBaseTask;
import org.narrative.network.customizations.narrative.service.mapper.util.ServiceMapperConfig;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.Set;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-07-16
 * Time: 11:15
 *
 * @author jonmark
 */
@Mapper(config = ServiceMapperConfig.class)
public abstract class EmailAddressMapper {
    @Mapping(source = EmailAddress.Fields.emailAddress, target = VerifyEmailAddressResultDTO.Fields.emailAddress)
    @Mapping(source = "incompleteVerificationSteps", target = VerifyEmailAddressResultDTO.Fields.incompleteVerificationSteps)
    // jw: these fields will be mapped by the map method below!
    @Mapping(ignore = true, target = VerifyEmailAddressResultDTO.Fields.emailAddressToVerify)
    @Mapping(ignore = true, target = VerifyEmailAddressResultDTO.Fields.token)
    public abstract VerifyEmailAddressResultDTO mapEmailAddressToVerifyEmailAddressResultDTO(EmailAddress emailAddress);

    @AfterMapping
    void map(EmailAddress emailAddress, @MappingTarget VerifyEmailAddressResultDTO.VerifyEmailAddressResultDTOBuilder builder) {
        User user = emailAddress.getUser();
        Set<EmailAddressVerificationStep> incompleteVerificationSteps = emailAddress.getIncompleteVerificationSteps();

        if (!incompleteVerificationSteps.isEmpty()) {
            assert incompleteVerificationSteps.size() == 1 : "Should only ever be one remaining verification step by the time we get here!";
            EmailAddressVerificationStep step = incompleteVerificationSteps.iterator().next();

            EmailAddress remainingEmailAddress = null;
            if (step.isVerifyPending()) {
                remainingEmailAddress = user.getUserFields().getPendingEmailAddress();
            } else {
                assert step.isVerifyPrimary() : "Expected verify primary, but found/"+this;

                // jw: only include the primary email address if the viewer is the user.
                if (user.isCurrentUserThisUser()) {
                    remainingEmailAddress = user.getUserFields().getEmailAddress();
                }
            }

            builder.emailAddressToVerify(exists(remainingEmailAddress) ? remainingEmailAddress.getEmailAddress() : null);
        }

        // jw: if all email types have been verified then the user should get a new Security token.
        //     If this was a verification of the active email address, they can now access their account, so new token.
        //     If it was a pending emailAddress, then it should now be active and they will definitely need a new token.
        // jw: only provide the token if the user for the email address is the same one making the request
        if (incompleteVerificationSteps.isEmpty() && user.isCurrentUserThisUser()) {
            assert emailAddress.getType().isPrimary() : "Regardless of the email addresses status at the start of this process, by now it should be the active email address on this users account.";

            builder.token(UpdateProfileAndJwtBaseTask.generateTokenDTO(user));
        }
    }
}
