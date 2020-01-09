package org.narrative.common.web;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Date: 6/29/17
 * Time: 1:17 PM
 *
 * @author brian
 */
public enum DateComponentType {
    MONTH,
    DAY,
    YEAR;

    public boolean isMonth() {
        return this == MONTH;
    }

    public boolean isDay() {
        return this == DAY;
    }

    public boolean isYear() {
        return this == YEAR;
    }

    public static List<DateComponentType> getDateFormatOrder(String dateFormat) {
        // bl: use a set to ensure we don't duplicate
        Set<DateComponentType> ret = new LinkedHashSet<>();
        for (char c : dateFormat.toCharArray()) {
            DateComponentType type;
            if (Character.toLowerCase(c) == 'm') {
                type = MONTH;
            } else if (Character.toLowerCase(c) == 'd') {
                type = DAY;
            } else if (Character.toLowerCase(c) == 'y') {
                type = YEAR;
            } else {
                continue;
            }
            addType(ret, type);
            if (ret.size() == values().length) {
                break;
            }
        }

        // bl: make sure that every type is included in the output!
        if (ret.size() < values().length) {
            for (DateComponentType type : values()) {
                addType(ret, type);
                if (ret.size() == values().length) {
                    break;
                }
            }
        }

        return new ArrayList<>(ret);
    }

    private static void addType(Set<DateComponentType> types, DateComponentType type) {
        if (types.contains(type)) {
            return;
        }
        types.add(type);
    }
}
