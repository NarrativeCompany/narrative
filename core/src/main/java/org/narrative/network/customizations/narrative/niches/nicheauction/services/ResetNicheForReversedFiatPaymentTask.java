package org.narrative.network.customizations.narrative.niches.nicheauction.services;

import org.narrative.network.core.area.user.AreaUserRlm;
import org.narrative.network.customizations.narrative.invoices.FiatPayment;
import org.narrative.network.customizations.narrative.invoices.services.ResetChannelForReversedFiatPaymentTaskBase;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.customizations.narrative.niches.niche.services.RevokeNicheOwnershipTask;
import org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuctionInvoice;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-05-31
 * Time: 14:10
 *
 * @author brian
 */
public class ResetNicheForReversedFiatPaymentTask extends ResetChannelForReversedFiatPaymentTaskBase<NicheAuctionInvoice, Niche> {

    public ResetNicheForReversedFiatPaymentTask(FiatPayment fiatPayment) {
        super(fiatPayment);
    }

    @Override
    protected Niche getChannelConsumer(NicheAuctionInvoice nicheInvoice) {
        return nicheInvoice.getAuction().getNiche();
    }

    @Override
    protected void resetChannelConsumer(Niche niche) {
        AreaUserRlm owner = niche.getOwner();

        assert exists(owner) : "We should always have an owner by the time we get here!";
        assert isEqual(owner.getUser(), fiatPayment.getInvoice().getUser()) : "The owner should always match the invoice.";

        // jw: let's allow the revoke niche ownership task to handle most of the processing for us. This includes clearing
        //     the moderator election and clearing the owner/invoices.
        getAreaContext().doAreaTask(new RevokeNicheOwnershipTask(niche));
    }
}
