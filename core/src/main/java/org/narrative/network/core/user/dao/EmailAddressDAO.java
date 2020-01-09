package org.narrative.network.core.user.dao;

import org.narrative.common.persistence.NameValuePair;
import org.narrative.common.persistence.OID;
import org.narrative.network.core.user.EmailAddress;
import org.narrative.network.core.user.EmailAddressType;
import org.narrative.network.core.user.User;
import org.narrative.network.shared.daobase.GlobalDAOImpl;

import java.time.Instant;

/**
 * Date: 2019-07-10
 * Time: 07:34
 *
 * @author jonmark
 */
public class EmailAddressDAO extends GlobalDAOImpl<EmailAddress, OID> {
    public EmailAddressDAO() {
        super(EmailAddress.class);
    }

    public EmailAddress getByEmailAddress(String emailAddress) {
        return getUniqueBy(new NameValuePair<>(EmailAddress.Fields.emailAddress, emailAddress));
    }

    public EmailAddress getForUserAndType(User user, EmailAddressType type) {
        return getUniqueBy(
                new NameValuePair<>(EmailAddress.Fields.user, user)
                ,new NameValuePair<>(EmailAddress.Fields.type, type)
        );
    }

    public int deleteExpiredPendingEmailAddresses(Instant expiration) {
        return getGSession()
                .getNamedQuery("emailAddress.deleteExpiredPendingEmailAddresses")
                .setParameter("expiration", expiration)
                .setParameter("pendingEmailAddressType", EmailAddressType.PENDING)
                .executeUpdate();
    }
}
