package org.narrative.network.shared.security;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.user.User;
import org.narrative.network.core.user.services.preferences.FormatPreferences;

import javax.persistence.Transient;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Nov 23, 2005
 * Time: 3:25:37 PM
 */
public interface Role extends Serializable {

    @Transient
    public abstract OID getOid();

    public abstract PrimaryRole getPrimaryRole();

    public abstract User getUser();

    public abstract FormatPreferences getFormatPreferences();

    public abstract String getDisplayNameResolved();

    public static final Comparator<Role> ROLE_DISPLAY_NAME_COMPARATOR = new Comparator<Role>() {
        public int compare(Role role1, Role role2) {
            int ret = role1.getDisplayNameResolved().compareToIgnoreCase(role2.getDisplayNameResolved());
            if (ret != 0) {
                return ret;
            }

            return OID.compareOids(role1.getOid(), role2.getOid());
        }
    };
}
