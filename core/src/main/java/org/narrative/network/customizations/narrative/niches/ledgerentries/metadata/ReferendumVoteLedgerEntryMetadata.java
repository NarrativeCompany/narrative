package org.narrative.network.customizations.narrative.niches.ledgerentries.metadata;

import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntryMetadata;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 2/15/18
 * Time: 1:39 PM
 */
public interface ReferendumVoteLedgerEntryMetadata extends LedgerEntryMetadata {
    boolean isVoteForReferendum();

    void setVoteForReferendum(boolean forReferendum);
}
