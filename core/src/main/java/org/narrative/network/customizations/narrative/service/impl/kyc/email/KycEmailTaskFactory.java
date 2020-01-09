package org.narrative.network.customizations.narrative.service.impl.kyc.email;

import org.narrative.network.core.user.User;
import org.narrative.network.core.user.UserKycEventType;
import org.narrative.network.customizations.narrative.service.api.model.kyc.UserKycStatus;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class KycEmailTaskFactory {
    private final Map<UserKycStatus, Class<? extends SendKycEmailTaskBase>> typeToClassMap;

    public KycEmailTaskFactory() {
        Map<UserKycStatus, Class<? extends SendKycEmailTaskBase>> map = new HashMap<>();
        map.put(UserKycStatus.APPROVED, SendKycCertifiedEmailTask.class);
        map.put(UserKycStatus.REJECTED, SendKycCertAttemptRejectedEmailTask.class);
        map.put(UserKycStatus.REVOKED, SendKycCertRevokedChargebackEmailTask.class);
        typeToClassMap = Collections.unmodifiableMap(map);

        // make sure we support every status in our map!
        for (UserKycEventType eventType : UserKycEventType.values()) {
            UserKycStatus status = eventType.getSendEmailForStatus();
            assert status==null || typeToClassMap.containsKey(status) : "Every sendEmailForStatus value should be handled in KycEmailTaskFactory! missing/" + status;
        }
    }

    public boolean eventTypeSendsEmail(UserKycEventType eventType) {
        // if there is a corresponding status for email for this event type, then that indicates it should receive an email!
        return eventType.getSendEmailForStatus()!=null;
    }

    @SuppressWarnings("unchecked")
    public <T extends SendKycEmailTaskBase> T buildEmailTask(UserKycEventType eventType, User user) {
        UserKycStatus status = eventType.getSendEmailForStatus();
        Class<? extends SendKycEmailTaskBase> emailTaskClass = status==null ? null : typeToClassMap.get(status);

        if (emailTaskClass == null) {
            throw new IllegalArgumentException("This event type does not support email generation: " + eventType.name());
        }

        try {
            return (T) emailTaskClass.getDeclaredConstructor(User.class, UserKycEventType.class).newInstance(user, eventType);
        } catch (Exception e) {
            throw new RuntimeException("Error constructing instance of email task class " + emailTaskClass.getSimpleName(), e);
        }
    }
}
