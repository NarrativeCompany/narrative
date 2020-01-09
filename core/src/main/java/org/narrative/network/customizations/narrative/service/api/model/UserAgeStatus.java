package org.narrative.network.customizations.narrative.service.api.model;

import org.narrative.network.core.user.UserKyc;

/**
 * Date: 2019-03-07
 * Time: 08:18
 *
 * @author brian
 */
public enum UserAgeStatus {
    UNKNOWN(null),
    UNDER_18(0),
    OVER_18(18),
    ;

    private final Integer minimumAge;

    UserAgeStatus(Integer minimumAge) {
        this.minimumAge = minimumAge;
    }

    public boolean isOver18() {
        return this==OVER_18;
    }

    public static UserAgeStatus getForUserKyc(UserKyc userKyc) {
        Integer age = userKyc.getAgeInYears();
        // bl: if there is no age, then the user's age is unknown
        if(age==null) {
            return UNKNOWN;
        }
        if(age>=OVER_18.minimumAge) {
            return OVER_18;
        }
        return UNDER_18;
    }
}
