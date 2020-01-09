package org.narrative.network.core.user.services;

import org.narrative.network.shared.util.NetworkLogger;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 3/21/17
 * Time: 8:46 AM
 * jw: currently this generic here is a bit strange, because we need to include the message for Android, but not for iOS. Thankfully, we can set that field to null for iOS and provide
 * a value for Android and it the same data can be used for both, in the future, we will likely need to revisit this if achieving the same task on both requires drastically different
 * data objects.
 */
public interface MobilePushNotificationTask {
    NetworkLogger logger = new NetworkLogger(MobilePushNotificationTask.class);

    String getNotificationMessage();

    String getTargetUrl();

}
