package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.NotNull;

/**
 * DTO representing notification settings for a {@link User}.
 */
@JsonValueObject
@JsonTypeName("UserNotificationSettings")
@Value
@Builder(toBuilder = true)
public class UserNotificationSettingsDTO {
    @NotNull
    final Boolean notifyWhenFollowed;
    @NotNull
    final Boolean notifyWhenMentioned;
    @NotNull
    final Boolean suspendAllEmails;
}
