package org.narrative.network.core.user.dao;

import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.narrative.common.persistence.NameValuePair;
import org.narrative.common.persistence.OID;
import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.narrative.wallet.Wallet;
import org.narrative.network.core.security.area.community.advanced.services.GlobalSecurable;
import org.narrative.network.core.user.AuthZone;
import org.narrative.network.core.user.EmailAddressType;
import org.narrative.network.core.user.User;
import org.narrative.network.shared.daobase.GlobalDAOImpl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Nov 28, 2005
 * Time: 11:05:17 AM
 */
public class UserDAO extends GlobalDAOImpl<User, OID> {

    public UserDAO() {
        super(User.class);
    }

    public User getByPrimaryEmailAddress(String emailAddress) {
        return (User) getGSession().getNamedQuery("user.getByPrimaryEmailAddress")
                .setParameter("emailAddress", emailAddress)
                .setParameter("primaryEmailAddressType", EmailAddressType.PRIMARY)
                .uniqueResult();
    }

    public ScrollableResults getAllNonDeletedScrollable() {
        return getGSession().getNamedQuery("user.getAllNonDeleted").scroll(ScrollMode.FORWARD_ONLY);
    }

    public List<User> getIndexRecordChunked(OID lastOid, int chunkSize) {
        return getGSession().getNamedQuery("user.getIndexRecordChunked").setParameter("lastOid", lastOid).setMaxResults(chunkSize).list();
    }

    public User getUserByUsername(AuthZone authZone, String username) {
        return getUniqueBy(new NameValuePair<>(User.FIELD__AUTH_ZONE__NAME, authZone), new NameValuePair<>(User.FIELD__USERNAME__NAME, username));
    }

    public Map<String,User> getUsersByUsername(AuthZone authZone, Collection<String> usernames) {
        if (isEmptyOrNull(usernames)) {
            return Collections.emptyMap();
        }
        List<User> users = getGSession()
                .getNamedQuery("user.getUsersByUsername")
                .setParameter("authZone", authZone)
                .setParameterList("usernames", usernames)
                .list();
        return users.stream().collect(Collectors.toMap(User::getUsername, Function.identity()));
    }

    public List<User> getAllUsersWithCommunityRights(Area area, GlobalSecurable... securables) {
        assert securables != null && securables.length > 0 : "Attempting to get users with securables without specifying securables";

        Set<Integer> securablesTypes = new HashSet<>();
        for (GlobalSecurable securable : securables) {
            assert securable.isAdminSecurable() : "Can only use getAllUsersWithCommunityRights with admin permissions! End-user permissions not supported currently, as that would mean integrating premium group permission support.";
            securablesTypes.add(securable.getId());
        }

        return getGSession().getNamedQuery("user.getAllUsersWithCommunityRights").setParameter("area", area).setParameterList("securableTypes", securablesTypes).list();
    }

    public int getWaitListInviteRank(User user) {
        if (user.getConfirmedWaitListInviteCount() == 0) {
            return 0;
        }

        Number higherRankedMemberCount = (Number) getGSession().getNamedQuery("user.getWaitListInviteRank").setParameter("authZone", user.getAuthZone()).setParameter("confirmedInviteCount", user.getConfirmedWaitListInviteCount()).setParameter("lastConfirmedInviteDatetime", user.getLastConfirmedWaitListInviteDatetime()).uniqueResult();

        return higherRankedMemberCount == null ? 1 : higherRankedMemberCount.intValue();
    }

    public Map<OID, User> getWalletOidToUser(Set<Wallet> wallets) {
        if(isEmptyOrNull(wallets)) {
            return Collections.emptyMap();
        }
        List<User> users = getAllIn(new NameValuePair<>(User.FIELD__WALLET__NAME, wallets));
        Map<OID, User> walletOidToUser = new HashMap<>();
        for (User user : users) {
            walletOidToUser.putIfAbsent(user.getWallet().getOid(), user);
        }
        return walletOidToUser;
    }
}

