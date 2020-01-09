package org.narrative.network.customizations.narrative.niches.nicheassociation;

import org.narrative.common.util.enums.IntegerEnum;

/**
 * Created by IntelliJ IDEA.
 * User: martin
 * Date: 12/02/2018
 * Time: 15:04
 */
public enum NicheAssociationSlot implements IntegerEnum {
    SLOT_1(0),
    SLOT_2(1),
    SLOT_3(2),
    SLOT_4(3),
    SLOT_5(4),
    SLOT_6(5),
    SLOT_7(6),
    SLOT_8(7),
    SLOT_9(8),
    SLOT_10(9);

    private final int id;

    NicheAssociationSlot(int id) {
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }
}
