package org.narrative.network.core.narrative.rewards;

import org.narrative.common.util.enums.IntegerEnum;

/**
 * Date: 9/30/19
 * Time: 8:10 AM
 *
 * @author brian
 */
public enum ContentCreatorRewardRole implements IntegerEnum {
    WRITER(0),
    PUBLICATION_OWNER(1),
    PUBLICATION_ADMIN(2),
    PUBLICATION_EDITOR(3)
    ;

    private final int id;

    ContentCreatorRewardRole(int id) {
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }

    public boolean isWriter() {
        return this==WRITER;
    }
}
