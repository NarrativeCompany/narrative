package org.narrative.network.customizations.narrative.service.api;

import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntry;
import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntryType;
import org.narrative.network.customizations.narrative.service.api.model.LedgerEntriesDTO;
import org.narrative.network.customizations.narrative.service.api.model.LedgerEntryDTO;
import org.narrative.network.customizations.narrative.util.LedgerEntryScrollable;

import java.util.Set;

/**
 * Date: 8/10/18
 * Time: 9:05 AM
 *
 * @author brian
 */
public interface LedgerEntryService {

    /**
     * Find a niche ledger entry by its OID.
     *
     * @param ledgerEntryOid {@link OID} specifying the ledger entry OID for the search
     * @return {@link LedgerEntryDTO} found, null otherwise
     */
    LedgerEntryDTO findLedgerEntryByOid(OID ledgerEntryOid);

    /**
     * Find channel ledger entries by channel OID.
     *
     * @param channelOid {@link OID} specifying the channel for the search
     * @param scrollable The number of items to return, and the cutoff for fetching more results
     * @return The {@link LedgerEntriesDTO} with {@link LedgerEntry}'s found
     */
    LedgerEntriesDTO findLedgerEntriesForChannel(OID channelOid, LedgerEntryScrollable scrollable);

    /**
     * Find all niche ledger entries by actor OID, page size and page.
     *
     * @param userOid {@link OID} specifying the actor for the search
     * @param ledgerEntryTypes Ledger entry types for the search
     * @param scrollable The number of items to return, and the cutoff for fetching more results
     * @return {@link LedgerEntriesDTO} with {@link LedgerEntry}'s found
     */
    LedgerEntriesDTO findLedgerEntriesForUser(OID userOid, Set<LedgerEntryType> ledgerEntryTypes, LedgerEntryScrollable scrollable);
}
