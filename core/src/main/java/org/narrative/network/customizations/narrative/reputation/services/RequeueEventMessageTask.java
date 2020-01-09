package org.narrative.network.customizations.narrative.reputation.services;

import org.narrative.network.customizations.narrative.reputation.EventMessage;

/**
 * Date: 2018-12-13
 * Time: 15:27
 *
 * @author jonmark
 */
public class RequeueEventMessageTask extends EventMessageRequeueTaskBase {
    public RequeueEventMessageTask(EventMessage event) {
        super(event);
    }

    @Override
    protected int getOriginalCount(EventMessage event) {
        return event.getRetryCount();
    }

    @Override
    protected void setNewCount(EventMessage event, int count) {
        event.setRetryCount(count);
    }
}
