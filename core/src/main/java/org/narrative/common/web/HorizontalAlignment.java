package org.narrative.common.web;

import org.narrative.common.util.enums.IntegerEnum;

import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: Feb 18, 2010
 * Time: 9:24:34 AM
 *
 * @author Jonmark Weber
 */
public enum HorizontalAlignment implements IntegerEnum {
    LEFT(0),
    CENTER(1),
    RIGHT(2);

    private final int id;

    private HorizontalAlignment(int id) {
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }

    public String getNameForDisplay() {
        return wordlet("horizontalAlignment." + this);
    }

    public boolean isLeft() {
        return this == LEFT;
    }

    public boolean isCenter() {
        return this == CENTER;
    }
}
