package org.narrative.network.core.area.user.services;

import org.narrative.network.core.area.user.AreaUser;
import org.narrative.network.core.area.user.AreaUserStats;
import org.narrative.network.core.composition.base.CompositionConsumer;
import org.narrative.network.core.composition.base.CompositionConsumerType;
import org.narrative.network.core.composition.base.Reply;
import org.narrative.network.core.user.User;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 4/21/16
 * Time: 8:27 AM
 * <p>
 * This task is meant to be run the first time a post goes live.  It will handle all of the AreaUserStats concerns, and
 * registering the appropriate AutomationRecipeTypes for processing.
 */
public class NewLivePostTask extends AreaTaskImpl<Object> {
    private final CompositionConsumer consumer;
    private final Reply reply;

    public NewLivePostTask(CompositionConsumer consumer) {
        assert exists(consumer) : "Should always provide a consumer to this constructor";
        this.consumer = consumer;
        this.reply = null;
    }

    public NewLivePostTask(CompositionConsumer consumer, Reply reply) {
        assert exists(consumer) : "Should always provide a consumer to this constructor";
        assert exists(reply) : "Should always provide a reply to this constructor";
        this.consumer = consumer;
        this.reply = reply;
    }

    @Override
    protected Object doMonitoredTask() {
        boolean forReply = exists(reply);
        CompositionConsumerType consumerType = consumer.getCompositionConsumerType();

        // jw: everything we do from here requires a user, so lets make sure there is one.
        // bl: these are used for stats and member makes first post recipes, so use the real author!
        User user = forReply ? reply.getRealAuthor() : consumer.getRealAuthor();
        AreaUser areaUser = null;
        if (exists(user)) {
            // jw: next, lets make sure that we have a AreaUser
            areaUser = user.getAreaUserByArea(consumer.getArea());
        }

        // jw: there is nothing left to do if the consumer type does not support authors.  Lets short out now if it doesnt.
        if (!consumerType.isSupportsAuthor()) {
            return null;
        }

        // jw: first, lets increment the post count if we have a AreaUser, and the consumer type supports it
        if (exists(areaUser)) {
            AreaUserStats aus = AreaUserStats.dao().getLocked(areaUser.getOid());
            if (forReply) {
                aus.addComment(consumer, reply);

            } else {
                aus.addNewContent(consumer);
            }
        }

        return null;
    }
}
