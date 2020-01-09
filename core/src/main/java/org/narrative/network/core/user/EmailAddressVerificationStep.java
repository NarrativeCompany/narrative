package org.narrative.network.core.user;

import org.narrative.common.util.enums.IntegerEnum;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Date: 2019-07-11
 * Time: 13:22
 *
 * @author jonmark
 */
public enum EmailAddressVerificationStep implements IntegerEnum {
    VERIFY_PRIMARY(0, "children-tempt-mortify-hackney-music-jettison-broil-tum-thrift-path")
    ,VERIFY_PENDING(1, "redbud-civilian-courtier-gloat-lantern-chicken-dross-cuticle-maternal-proctor")
    ;

    public static final EmailAddressVerificationStep NEW_USER_STEP = VERIFY_PRIMARY;

    public static final String TYPE = "org.narrative.network.core.user.EmailAddressVerificationStep";

    public static final Set<EmailAddressVerificationStep> ALL = Collections.unmodifiableSet(EnumSet.allOf(EmailAddressVerificationStep.class));

    private final int id;
    private final String privateKey;

    EmailAddressVerificationStep(int id, String privateKey) {
        this.id = id;
        this.privateKey = privateKey;
    }

    @Override
    public int getId() {
        return id;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public boolean isVerifyPrimary() {
        return this == VERIFY_PRIMARY;
    }

    public boolean isVerifyPending() {
        return this == VERIFY_PENDING;
    }

    public boolean isNewUserStep() {
        return this == NEW_USER_STEP;
    }
}