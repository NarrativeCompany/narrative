package org.narrative.network.customizations.narrative.elections;

import org.narrative.common.util.enums.IntegerEnum;

/**
 * Date: 11/12/18
 * Time: 10:04 AM
 *
 * @author jonmark
 */
public enum ElectionStatus implements IntegerEnum {
    NOMINATING(0),
    VOTING(1),
    CANCELED(2),
    COMPLETED(3),
    ;

    private final int id;

    ElectionStatus(int id) {
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }

    public boolean isNominating() {
        return this == NOMINATING;
    }

    public boolean isVoting() {
        return this == VOTING;
    }

    public boolean isOpen() {
        return isNominating() || isVoting();
    }
}