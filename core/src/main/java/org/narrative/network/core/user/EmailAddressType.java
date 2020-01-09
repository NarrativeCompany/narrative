package org.narrative.network.core.user;

import org.narrative.common.util.enums.IntegerEnum;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Date: 2019-07-10
 * Time: 08:00
 *
 * @author jonmark
 */
public enum EmailAddressType implements IntegerEnum {
    PRIMARY(0)
    ,PENDING(1)
    ;

    public static final String TYPE = "org.narrative.network.core.user.EmailAddressType";

    public static final Set<EmailAddressType> ALL = Collections.unmodifiableSet(EnumSet.allOf(EmailAddressType.class));
    public static final Set<EmailAddressType> ACTIVE_ONLY = Collections.unmodifiableSet(EnumSet.of(EmailAddressType.PRIMARY));

    private final int id;

    EmailAddressType(int id) {
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }

    public boolean isPrimary() {
        return this == PRIMARY;
    }

    public boolean isPending() {
        return this == PENDING;
    }
}