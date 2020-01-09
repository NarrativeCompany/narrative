package org.narrative.network.customizations.narrative.controller.postbody.user;

import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.NotNull;

@Value
@Builder
public class UserNotificationSettingsInputDTO {
    @NotNull
    final Boolean notifyWhenFollowed;
    @NotNull
    final Boolean notifyWhenMentioned;
    @NotNull
    final Boolean suspendAllEmails;
}
