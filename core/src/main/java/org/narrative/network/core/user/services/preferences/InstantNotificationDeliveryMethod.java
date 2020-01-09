package org.narrative.network.core.user.services.preferences;

import org.narrative.common.util.NameForDisplayProvider;
import org.narrative.common.util.enums.IntegerEnum;
import org.narrative.network.core.user.User;
import org.narrative.network.core.user.services.MobilePushNotificationTask;
import org.narrative.network.shared.email.NetworkMailUtil;
import org.narrative.network.shared.email.ReplyToEmailAddressOverrideProvider;
import org.narrative.network.shared.tasktypes.NetworkTaskImpl;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 3/10/17
 * Time: 12:32 PM
 */
public enum InstantNotificationDeliveryMethod implements IntegerEnum, NameForDisplayProvider {
    EMAIL(0) {
        @Override
        public <T extends NetworkTaskImpl & MobilePushNotificationTask> void sendInstantNotification(T task, User user) {
            // jw: we will go ahead and send the email right away
            if (task instanceof ReplyToEmailAddressOverrideProvider) {
                ReplyToEmailAddressOverrideProvider replyAddressOverride = (ReplyToEmailAddressOverrideProvider) task;
                NetworkMailUtil.sendJspCreatedEmail(task, user, null, null, replyAddressOverride.getReplyToEmailAddressOverride(), null);

            } else {
                NetworkMailUtil.sendJspCreatedEmail(task, user);
            }
        }
    };

    public static final Set<InstantNotificationDeliveryMethod> BASE_METHODS = Collections.unmodifiableSet(EnumSet.of(EMAIL));

    public static final String TYPE = "org.narrative.network.core.user.services.preferences.InstantNotificationDeliveryMethod";

    private final int id;

    InstantNotificationDeliveryMethod(int id) {
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getNameForDisplay() {
        return wordlet("instantNotificationDeliveryMethod." + this);
    }

    public boolean isEmail() {
        return this == EMAIL;
    }

    public abstract <T extends NetworkTaskImpl & MobilePushNotificationTask> void sendInstantNotification(T task, User user);
}
