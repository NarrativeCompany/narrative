package org.narrative.reputation.model.entity;

import java.util.HashMap;
import java.util.Map;

public enum RollupPeriod {
    DAILY(0),
    WEEKLY(1),
    MONTHLY(2);

    private static final Map<Integer, RollupPeriod> map = new HashMap<>();
    private final int ordinalValue;

    static {
        for (RollupPeriod status : RollupPeriod.values()) {
            map.put(status.getOrdinalValue(), status);
        }
    }

    RollupPeriod(int ordinalValue) {
        this.ordinalValue = ordinalValue;
    }

    public int getOrdinalValue() {
        return ordinalValue;
    }

    public static RollupPeriod getRollupPeriod(int value) {
        return map.get(value);
    }
}
