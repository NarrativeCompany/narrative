package org.narrative.network.customizations.narrative.service.impl.narrativepost;

import org.narrative.network.core.content.base.Content;
import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntry;
import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntryType;
import org.narrative.network.customizations.narrative.niches.ledgerentries.services.SaveLedgerEntryTask;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

/**
 * Date: 10/3/19
 * Time: 7:38 AM
 *
 * @author brian
 */
public class SaveNewPostLedgerEntry extends AreaTaskImpl<LedgerEntry> {
    private final Content content;

    public SaveNewPostLedgerEntry(Content content) {
        this.content = content;
    }

    @Override
    protected LedgerEntry doMonitoredTask() {
        LedgerEntry ledgerEntry = new LedgerEntry(content.getAreaUserRlm(), LedgerEntryType.USER_PUBLISHED_POST);
        ledgerEntry.setContentOid(content.getOid());
        return getNetworkContext().doGlobalTask(new SaveLedgerEntryTask(ledgerEntry));
    }
}
