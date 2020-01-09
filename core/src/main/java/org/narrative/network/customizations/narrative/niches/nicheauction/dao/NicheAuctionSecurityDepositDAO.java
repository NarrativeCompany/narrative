package org.narrative.network.customizations.narrative.niches.nicheauction.dao;

import org.narrative.common.persistence.*;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuction;
import org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuctionSecurityDeposit;
import org.narrative.network.shared.daobase.GlobalDAOImpl;

import java.util.List;

/**
 * Date: 2019-04-16
 * Time: 13:26
 *
 * @author jonmark
 */
public class NicheAuctionSecurityDepositDAO extends GlobalDAOImpl<NicheAuctionSecurityDeposit, OID> {
    public NicheAuctionSecurityDepositDAO() {
        super(NicheAuctionSecurityDeposit.class);
    }

    public List<OID> getAllSecurityDepositOidsForAuctionExcludingUser(NicheAuction auction, User user) {
        return getGSession().createNamedQuery("nicheAuctionSecurityDeposit.getAllSecurityDepositOidsForAuctionExcludingUser", OID.class)
                .setParameter("auction", auction)
                .setParameter("user", user)
                .list();
    }

    public NicheAuctionSecurityDeposit getSecurityDeposit(NicheAuction auction, User user) {
        return getUniqueBy(
                new NameValuePair<>(NicheAuctionSecurityDeposit.Fields.auction, auction),
                new NameValuePair<>(NicheAuctionSecurityDeposit.Fields.user, user)
        );
    }
}
