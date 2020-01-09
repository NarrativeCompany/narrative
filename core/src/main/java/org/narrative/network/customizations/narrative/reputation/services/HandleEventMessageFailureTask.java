package org.narrative.network.customizations.narrative.reputation.services;

import org.narrative.network.customizations.narrative.reputation.EventMessage;

/**
 * Date: 2018-12-13
 * Time: 15:32
 *
 * @author jonmark
 */
public class HandleEventMessageFailureTask extends EventMessageRequeueTaskBase {
    public HandleEventMessageFailureTask(EventMessage event) {
        super(event);
    }

    @Override
    protected int getOriginalCount(EventMessage event) {
        return event.getFailCount();
    }

    @Override
    protected void setNewCount(EventMessage event, int count) {
        event.setFailCount(count);
    }
}
