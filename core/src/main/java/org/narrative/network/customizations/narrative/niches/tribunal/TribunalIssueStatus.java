package org.narrative.network.customizations.narrative.niches.tribunal;

import org.narrative.common.util.enums.IntegerEnum;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 3/6/18
 * Time: 4:45 PM
 */
public enum TribunalIssueStatus implements IntegerEnum {
    OPEN(0);

    private final int id;

    TribunalIssueStatus(int id) {
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }
}
