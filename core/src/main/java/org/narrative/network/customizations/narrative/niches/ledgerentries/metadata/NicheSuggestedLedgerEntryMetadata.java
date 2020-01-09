package org.narrative.network.customizations.narrative.niches.ledgerentries.metadata;

import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntryMetadata;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 2/15/18
 * Time: 1:29 PM
 */
public interface NicheSuggestedLedgerEntryMetadata extends LedgerEntryMetadata {
    String getNicheName();
    void setNicheName(String name);

    String getNicheDescription();
    void setNicheDescription(String description);
}
