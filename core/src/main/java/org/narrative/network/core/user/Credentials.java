package org.narrative.network.core.user;

import org.narrative.common.persistence.OID;

/**
 * Date: Sep 30, 2010
 * Time: 8:34:07 AM
 *
 * @author brian
 */
public interface Credentials {
    public OID getOid();

    public String getEmailAddress();

    public void setEmailAddress(String emailAddress);

    public boolean isEmailVerified();

    public PasswordFields getPasswordFields();

    public User getUser();

    public AuthZone getAuthZone();
}
