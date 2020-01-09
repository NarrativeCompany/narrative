package org.narrative.network.customizations.narrative.reputation;

import org.narrative.common.util.enums.IntegerEnum;

/**
 * Date: 2018-12-11
 * Time: 13:06
 *
 * @author jonmark
 */
public enum EventMessageStatus implements IntegerEnum {
    QUEUED(0),
    SENT(1),
    FAILED_PROCESSING(2);

    private final int id;

    EventMessageStatus(int id) {
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }
}