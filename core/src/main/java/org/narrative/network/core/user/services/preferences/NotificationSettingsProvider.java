package org.narrative.network.core.user.services.preferences;

import org.narrative.common.persistence.Bitmask;
import org.narrative.common.persistence.LongBitmaskType;

import java.util.Collection;

/**
 * Date: Nov 11, 2010
 * Time: 1:54:41 PM
 *
 * @author brian
 */
public interface NotificationSettingsProvider<T extends Enum<T> & Bitmask<LongBitmaskType<T>>> {
    Collection<T> getAllNotificationTypes();
    LongBitmaskType<T> getNotificationSettings();
}
