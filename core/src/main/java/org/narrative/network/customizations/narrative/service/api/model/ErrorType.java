package org.narrative.network.customizations.narrative.service.api.model;

import java.util.EnumSet;

/**
 * Date: 8/24/18
 * Time: 9:06 AM
 *
 * @author brian
 */
public enum ErrorType {
    UNKNOWN_ERROR
    ,JWT_INVALID
    ,JWT_2FA_EXPIRED
    ,NOT_FOUND
    ,LOGIN_REQUIRED
    ,ACCESS_DENIED
    ,TOS_AGREEMENT_REQUIRED
    ,EMAIL_VERIFICATION_REQUIRED
    ,ACTIVITY_RATE_LIMIT_EXCEEDED
    ,EXPIRED_PUBLICATION
    ;

    public static EnumSet<ErrorType> JWT_INVALID_TYPES = EnumSet.of(JWT_INVALID, JWT_2FA_EXPIRED);
}
