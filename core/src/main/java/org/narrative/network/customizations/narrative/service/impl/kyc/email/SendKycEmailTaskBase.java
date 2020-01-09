package org.narrative.network.customizations.narrative.service.impl.kyc.email;

import org.narrative.network.core.user.User;
import org.narrative.network.core.user.UserKycEventType;
import org.narrative.network.customizations.narrative.niches.services.SendSingleNarrativeEmailTaskBase;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public abstract class SendKycEmailTaskBase extends SendSingleNarrativeEmailTaskBase {
    private final UserKycEventType eventType;

    SendKycEmailTaskBase(User user, UserKycEventType eventType) {
        super(user);
        this.eventType = eventType;
        assert isSupportedEventType(eventType) : "The event type " + eventType.name() + " is not supported by this task " + getClass().getSimpleName();
    }

    protected abstract boolean isSupportedEventType(UserKycEventType eventType);
}
