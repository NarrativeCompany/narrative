package org.narrative.network.customizations.narrative.service.impl.kyc.email;

import org.narrative.network.core.user.User;
import org.narrative.network.core.user.UserKycEventType;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SendKycCertAttemptRejectedEmailTask extends SendKycEmailTaskBase {
    SendKycCertAttemptRejectedEmailTask(User user, UserKycEventType eventType) {
        super(user, eventType);
    }

    @Override
    protected boolean isSupportedEventType(UserKycEventType eventType) {
        return eventType.getSendEmailForStatus().isRejected();
    }
}
