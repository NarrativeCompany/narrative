package org.narrative.network.customizations.narrative.niches.nicheauction.services;

import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuctionBid;
import org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuctionSecurityDeposit;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import org.narrative.network.shared.tasktypes.TaskRunner;

import java.util.List;

/**
 * Date: 2019-04-17
 * Time: 07:53
 *
 * @author jonmark
 */
public class RefundNicheAuctionLosingBiddersTask extends AreaTaskImpl<Object> {
    private final NicheAuctionBid winningBid;

    public RefundNicheAuctionLosingBiddersTask(NicheAuctionBid winningBid) {
        this.winningBid = winningBid;
    }

    @Override
    protected List<OID> doMonitoredTask() {
        List<OID> securityDepositOids = NicheAuctionSecurityDeposit.dao().getAllSecurityDepositOidsForAuctionExcludingUser(
                winningBid.getAuction(),
                winningBid.getBidder().getUser()
        );

        for (OID securityDepositOid : securityDepositOids) {
            // jw: let's process each security deposit in its own transaction
            TaskRunner.doRootAreaTask(getAreaContext().getArea().getOid(), new AreaTaskImpl<Object>() {
                @Override
                protected Object doMonitoredTask() {

                    return getAreaContext().doAreaTask(new RefundNicheAuctionSecurityDepositTask(
                            NicheAuctionSecurityDeposit.dao().get(securityDepositOid))
                    );
                }
            });
        }

        return null;
    }
}
