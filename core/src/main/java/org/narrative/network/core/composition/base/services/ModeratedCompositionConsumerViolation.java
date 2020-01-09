package org.narrative.network.core.composition.base.services;

import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.composition.base.CompositionConsumer;
import org.narrative.network.core.security.area.base.AreaRole;
import org.narrative.network.shared.security.AccessViolation;
import org.narrative.network.shared.security.PrimaryRole;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 7/21/16
 * Time: 2:08 PM
 */
public class ModeratedCompositionConsumerViolation extends AccessViolation {
    public ModeratedCompositionConsumerViolation(CompositionConsumer consumer, AreaRole areaRole) {
        super(wordlet("moderatedCompositionConsumerViolation.consumerNotVisible", consumer.getTypeLowercaseNameForDisplay()));
    }

    public static void checkViewRight(CompositionConsumer consumer, AreaRole areaRole) {
        // jw: for moderation, we want to throw the ViewCompositionConsumerViolation if the viewer cannot moderate.
        if (consumer.isModerated()) {
            if (!consumer.isManageableByAreaRole(areaRole)) {
                // jw: if the AreaRole is the author, then we should use the CompositionConsumerAccessViolation which will give a more accurate message!
                PrimaryRole primaryRole = areaRole.getPrimaryRole();
                if (primaryRole.isRegisteredUser()) {
                    if (isEqual(consumer.getRealAuthor(), primaryRole.getUser())) {
                        throw new CompositionConsumerAccessViolation(consumer);
                    }
                }
                throw new ModeratedCompositionConsumerViolation(consumer, areaRole);
            }
        } else if (consumer.isDraft()) {
            // bl: for drafts, only the author should be able to edit (unless there is no author, then any mods can)
            if (!exists(consumer.getRealAuthor())) {
                consumer.checkManageable(areaRole);
            } else if(!isEqual(consumer.getRealAuthor(), areaRole.getUser())) {
                throw new AccessViolation(wordlet("post.accessViolation"));
            }

            // jw: if the consumer is not live and nothing above hit then something else is disabled, lets prevent access here.
        } else if (!consumer.isLive()) {
            throw UnexpectedError.getIgnorableRuntimeException("Should never attempt to access content that is not live!");
        }
    }
}
