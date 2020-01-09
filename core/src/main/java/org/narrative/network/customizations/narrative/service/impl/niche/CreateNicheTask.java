package org.narrative.network.customizations.narrative.service.impl.niche;

import org.narrative.network.customizations.narrative.channels.Channel;
import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntry;
import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntryType;
import org.narrative.network.customizations.narrative.niches.ledgerentries.metadata.NicheSuggestedLedgerEntryMetadata;
import org.narrative.network.customizations.narrative.niches.ledgerentries.services.SaveLedgerEntryTask;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.customizations.narrative.niches.niche.NicheStatus;
import org.narrative.network.customizations.narrative.niches.referendum.Referendum;
import org.narrative.network.customizations.narrative.niches.referendum.ReferendumType;
import org.narrative.network.customizations.narrative.niches.referendum.services.CreateReferendumTask;
import org.narrative.network.customizations.narrative.service.api.model.input.CreateNicheRequest;

import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: 10/18/18
 * Time: 8:22 AM
 *
 * @author brian
 */
public class CreateNicheTask extends SubmitNicheDetailsBaseTask<Referendum,CreateNicheRequest> {
    CreateNicheTask(CreateNicheRequest createNicheRequest) {
        super(createNicheRequest);
    }

    @Override
    protected Referendum doMonitoredTask() {
        // jw: create the niche
        Niche niche = new Niche(nicheInput.getName(),
                                nicheInput.getDescription(),
                                Niche.dao().getAvailablePrettyUrlString(getAreaContext().getPortfolio(), nicheInput.getName()),
                                NicheStatus.SUGGESTED,
                                getAreaContext().getAreaUserRlm(),
                                getAreaContext().getPortfolio());
        Niche.dao().save(niche);

        // jw: now that the Niche has been saved, let's store the Channel. We need to do this after since the Niche
        //     needs to have its OID generated first.
        Channel channel = new Channel(niche);
        Channel.dao().save(channel);
        niche.setChannel(channel);

        // jw: create the referendum for the community to approve/reject this niche
        Referendum referendum = getAreaContext().doAreaTask(new CreateReferendumTask(niche, ReferendumType.APPROVE_SUGGESTED_NICHE));

        // jw: add the ledger entry
        LedgerEntry entry = new LedgerEntry(getAreaContext().getAreaUserRlm(), LedgerEntryType.NICHE_SUGGESTED);
        entry.setChannelForConsumer(niche);
        entry.setReferendum(referendum);
        NicheSuggestedLedgerEntryMetadata metadata = entry.getMetadata();
        metadata.setNicheName(niche.getName());
        metadata.setNicheDescription(niche.getDescription());
        networkContext().doGlobalTask(new SaveLedgerEntryTask(entry));

        return referendum;
    }
}
