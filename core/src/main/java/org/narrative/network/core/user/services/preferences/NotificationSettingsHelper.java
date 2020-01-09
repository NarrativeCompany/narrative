package org.narrative.network.core.user.services.preferences;

import org.narrative.common.persistence.Bitmask;
import org.narrative.common.persistence.LongBitmaskType;

/**
 * Date: Nov 11, 2010
 * Time: 1:53:35 PM
 *
 * @author brian
 */
public class NotificationSettingsHelper<T extends Enum<T> & Bitmask<LongBitmaskType<T>>> {
    private final NotificationSettingsProvider<T> provider;

    public NotificationSettingsHelper(NotificationSettingsProvider<T> provider) {
        this.provider = provider;
    }

    public void setNotificationFlag(T notificationType, boolean isOn) {
        if (isOn) {
            provider.getNotificationSettings().turnOn(notificationType);
        } else {
            provider.getNotificationSettings().turnOff(notificationType);
        }
    }

    public boolean isNotificationSet(T type) {
        return provider.getNotificationSettings().isThis(type);
    }

    public void setDefaultNotificationPreferences() {
        for (T notificationType : provider.getAllNotificationTypes()) {
            setNotificationFlag(notificationType, true);
        }
    }
}
