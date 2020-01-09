package org.narrative.network.customizations.narrative.niches.ledgerentries.dao;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.area.user.AreaUserRlm;
import org.narrative.network.customizations.narrative.channels.Channel;
import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntry;
import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntryType;
import org.narrative.network.shared.daobase.GlobalDAOImpl;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: martin
 * Date: 12/2/2018
 * Time: 09:20
 */
public class LedgerEntryDAO extends GlobalDAOImpl<LedgerEntry, OID> {
    public LedgerEntryDAO() {
        super(LedgerEntry.class);
    }

    public List<LedgerEntry> getEntriesForChannelBefore(Channel channel, Set<LedgerEntryType> ledgerEntryTypes, Instant before, int count) {
        return getGSession()
                .getNamedQuery("ledgerEntry.getEntriesForChannelBefore")
                .setParameter("channel", channel)
                .setParameterList("ledgerEntryTypes", ledgerEntryTypes)
                .setParameter("before", before != null ? before : Instant.now())
                .setMaxResults(count)
                .list();
    }

    public List<LedgerEntry> getEntriesForAreaUserRlm(AreaUserRlm areaUserRlm, Timestamp before, int results) {
        // jw: let's ensure that we have a value for the before value
        Instant beforeInstant;
        if (before == null) {
            beforeInstant = Instant.now();

        } else {
            beforeInstant = Instant.ofEpochMilli(before.getTime());
        }

        return getGSession().getNamedQuery("ledgerEntry.getEntriesForAreaUserRlm").setParameter("areaUserRlm", areaUserRlm).setParameter("before", beforeInstant).setMaxResults(results).list();
    }

    public List<LedgerEntry> getEntriesForAreaUserRlmByOid(OID areaUserRlmOid, Set<LedgerEntryType> ledgerEntryTypes, Instant before, int resultsPerPage) {
        return getGSession()
                .getNamedQuery("ledgerEntry.getEntriesForAreaUserRlmByOid")
                .setParameter("areaUserRlmOid", areaUserRlmOid)
                .setParameterList("ledgerEntryTypes", ledgerEntryTypes)
                .setParameter("before", before != null ? before : Instant.now())
                .setMaxResults(resultsPerPage)
                .list();
    }

    public void removeReferencesForDeletedChannel(Channel channel) {
        getGSession().getNamedQuery("ledgerEntry.removeReferencesForDeletedChannel")
                .setParameter("channel", channel)
                .executeUpdate();
    }
}