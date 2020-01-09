package org.narrative.network.customizations.narrative.posts;

import org.narrative.common.util.enums.IntegerEnum;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Date: 2019-01-03
 * Time: 12:12
 *
 * @author jonmark
 */
public enum NarrativePostStatus implements IntegerEnum {
    APPROVED(0)
    ,BLOCKED(1)
    ,MODERATED(2)
    ;

    private final int id;

    NarrativePostStatus(int id) {
        this.id = id;
    }

    public static final Set<NarrativePostStatus> NON_BLOCKED_STATUSES = Collections.unmodifiableSet(EnumSet.of(APPROVED, MODERATED));

    @Override
    public int getId() {
        return id;
    }

    public boolean isApproved() {
        return this == APPROVED;
    }

    public boolean isBlocked() {
        return this == BLOCKED;
    }

    public boolean isModerated() {
        return this == MODERATED;
    }
}