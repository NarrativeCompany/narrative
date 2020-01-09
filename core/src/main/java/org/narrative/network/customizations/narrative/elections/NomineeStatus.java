package org.narrative.network.customizations.narrative.elections;

import org.narrative.common.util.enums.IntegerEnum;
import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntryType;

/**
 * Date: 11/13/18
 * Time: 9:03 AM
 *
 * @author jonmark
 */
public enum NomineeStatus implements IntegerEnum {
    PENDING(0),
    CONFIRMED(1) {
        @Override
        public LedgerEntryType getLedgerEntryType(ElectionType electionType) {
            return electionType.getNomineeConfirmedEntryType();
        }
    },
    WITHDRAWN(2) {
        @Override
        public LedgerEntryType getLedgerEntryType(ElectionType electionType) {
            return electionType.getNomineeWithdrawnEntryType();
        }
    },
    // jw: this represents when someone declined an invitation to be a nominee from another user.
    DECLINED(3),
    ;

    private final int id;

    NomineeStatus(int id) {
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }

    public LedgerEntryType getLedgerEntryType(ElectionType electionType) {
        return null;
    }

    public boolean isConfirmed() {
        return this == CONFIRMED;
    }

    public boolean isPending() {
        return this == PENDING;
    }

    public boolean isWithdrawn() {
        return this == WITHDRAWN;
    }

    public boolean isDeclined() {
        return this == DECLINED;
    }

    public boolean isNegativeType() {
        return isWithdrawn() || isDeclined();
    }
}