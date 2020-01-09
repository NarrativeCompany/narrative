package org.narrative.network.customizations.narrative.service.api.model.kyc;

import org.narrative.common.util.enums.IntegerEnum;

/**
 * Date: 1/2/18
 * Time: 3:40 PM
 *
 * @author brian
 */
public enum KycIdentificationType implements IntegerEnum {
    // bl: always require the back image for driver's licenses and government-issue IDs. only passports don't require it.
    PASSPORT(0, false),
    DRIVERS_LICENSE(1, true),
    GOVERNMENT_ID(2, true);

    private final int id;
    private final boolean requiresBackImage;

    KycIdentificationType(int id, boolean requiresBackImage) {
        this.id = id;
        this.requiresBackImage = requiresBackImage;
    }

    @Override
    public int getId() {
        return id;
    }

    public boolean isRequiresBackImage() {
        return requiresBackImage;
    }
}