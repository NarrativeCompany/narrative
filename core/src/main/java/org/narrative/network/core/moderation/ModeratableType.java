package org.narrative.network.core.moderation;

import org.narrative.common.util.enums.IntegerEnum;
import org.narrative.common.util.enums.StringEnum;
import org.narrative.network.core.composition.base.CompositionType;

/**
 * Date: 9/29/11
 * Time: 11:17 AM
 *
 * @author brian
 */
public enum ModeratableType implements IntegerEnum, StringEnum {
    CONTENT(0, "CONTENT", CompositionType.CONTENT);

    private final int id;
    private final String idStr;
    private final CompositionType compositionType;

    ModeratableType(int id, String idStr, CompositionType compositionType) {
        this.id = id;
        this.idStr = idStr;
        this.compositionType = compositionType;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getIdStr() {
        return idStr;
    }

    public CompositionType getCompositionType() {
        return compositionType;
    }

    public boolean isContent() {
        return this == CONTENT;
    }

}
