package org.narrative.network.core.user;

import org.mindrot.jbcrypt.BCrypt;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * Date: Sep 27, 2010
 * Time: 9:55:34 AM
 *
 * @author brian
 */
@Embeddable
public class PasswordFields {
    public static final String PASSWORD_PARAM = "password";
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_PASSWORD_LENGTH = 40;

    private String hashedPassword;

    /**
     * the new, one-way encrypted password.  good luck decrypting it.
     *
     * @return the hashed version of the password.
     */
    @Column(nullable = false)
    public String getHashedPassword() {
        return hashedPassword;
    }

    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    public boolean isCorrectPassword(String password) {
        return BCrypt.checkpw(password, getHashedPassword());
    }

    public void setPassword(String password) {
        setHashedPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
    }
}
