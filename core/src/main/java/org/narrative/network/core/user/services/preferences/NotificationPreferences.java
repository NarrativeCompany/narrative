package org.narrative.network.core.user.services.preferences;

import org.narrative.common.persistence.Bitmask;
import org.narrative.common.persistence.LongBitmaskType;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

/**
 * Date: 8/14/12
 * Time: 9:17 AM
 * User: jonmark
 */
@MappedSuperclass
public abstract class NotificationPreferences<T extends Enum<T> & Bitmask<LongBitmaskType<T>>> implements NotificationSettingsProvider<T> {
    private LongBitmaskType<T> notificationSettings;

    /**
     * @deprecated for hibernate use only
     */
    public NotificationPreferences() {}

    public NotificationPreferences(boolean init) {
        if (init) {
            notificationSettings = new LongBitmaskType<T>();
        }
    }

    @Override
    @Column(nullable = false)
    public LongBitmaskType<T> getNotificationSettings() {
        return notificationSettings;
    }

    public void setNotificationSettings(LongBitmaskType<T> notificationSettings) {
        this.notificationSettings = notificationSettings;
    }
}
