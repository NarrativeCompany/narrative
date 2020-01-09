package org.narrative.network.core.area.user;

import org.narrative.network.core.user.services.preferences.AreaNotificationType;
import org.narrative.network.core.user.services.preferences.NotificationPreferences;

import javax.persistence.Embeddable;
import javax.persistence.Transient;

import java.util.Collection;
import java.util.EnumSet;

/**
 * Date: Nov 11, 2010
 * Time: 8:03:04 PM
 *
 * @author brian
 */
@Embeddable
public class AreaUserPreferences extends NotificationPreferences<AreaNotificationType> {

    public AreaUserPreferences() {}

    public AreaUserPreferences(boolean init) {
        super(init);
    }

    @Transient
    @Override
    public Collection<AreaNotificationType> getAllNotificationTypes() {
        return EnumSet.allOf(AreaNotificationType.class);
    }
}
