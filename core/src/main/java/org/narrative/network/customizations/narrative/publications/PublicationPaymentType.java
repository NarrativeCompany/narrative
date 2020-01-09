package org.narrative.network.customizations.narrative.publications;

import org.narrative.common.util.enums.*;

/**
 * Date: 2019-08-22
 * Time: 10:06
 *
 * @author jonmark
 */
public enum PublicationPaymentType implements IntegerEnum {
    INITIAL(0),
    RENEWAL(1),
    UPGRADE(2),
    ;

    private final int id;

    PublicationPaymentType(int id) {
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }

    public static PublicationPaymentType getPaymentType(boolean isInTrial, boolean isUpgrade) {
        if (isInTrial) {
            return INITIAL;
        }

        return isUpgrade ? UPGRADE : RENEWAL;
    }
}