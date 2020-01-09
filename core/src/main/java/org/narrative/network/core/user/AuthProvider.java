package org.narrative.network.core.user;

import org.narrative.common.util.enums.IntegerEnum;

/**
 * Date: Sep 23, 2010
 * Time: 8:16:32 AM
 *
 * @author brian
 */
public enum AuthProvider implements IntegerEnum {
    SANDBOXED_AREA(3);

    private final int id;

    AuthProvider(int id) {
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }

}
