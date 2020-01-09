package org.narrative.network.customizations.narrative.publications.dao;

import org.narrative.common.persistence.NameValuePair;
import org.narrative.common.persistence.OID;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.publications.Publication;
import org.narrative.network.customizations.narrative.publications.PublicationWaitListEntry;
import org.narrative.network.shared.daobase.GlobalDAOImpl;
import org.hibernate.query.Query;

import javax.persistence.LockModeType;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 9/23/19
 * Time: 12:52 PM
 *
 * @author brian
 */
public class PublicationWaitListEntryDAO extends GlobalDAOImpl<PublicationWaitListEntry, OID> {
    public PublicationWaitListEntryDAO() {
        super(PublicationWaitListEntry.class);
    }

    public PublicationWaitListEntry getForEmailAddress(String emailAddress) {
        return getFirstBy(new NameValuePair<>(PublicationWaitListEntry.Fields.emailAddress, emailAddress));
    }

    public PublicationWaitListEntry getWaitListEntryLocked(User user) {
        DAOObjectQueryBuilder builder = daoObjectQueryBuilder();
        builder.nameValuePairs(new NameValuePair[] {new NameValuePair<>(PublicationWaitListEntry.Fields.emailAddress, user.getEmailAddress())});
        DAOObjectQuery daoObjectQuery = builder.build();
        Query<PublicationWaitListEntry> query = daoObjectQuery.getQuery();
        // bl: set the lock mode to avoid concurrency issues
        return query.setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .uniqueResult();
    }

    public boolean isUserEligibleForDiscount(User user) {
        PublicationWaitListEntry entry = getFirstBy(new NameValuePair<>(PublicationWaitListEntry.Fields.emailAddress, user.getEmailAddress()));
        return exists(entry) && !entry.isUsed();
    }

    public boolean isPublicationEligibleForDiscount(Publication publication) {
        return exists(getFirstBy(new NameValuePair<>(PublicationWaitListEntry.Fields.publication, publication)));
    }

    public void deleteForPublication(Publication publication) {
        deleteAllByPropertyValue(PublicationWaitListEntry.Fields.publication, publication);
    }
}
