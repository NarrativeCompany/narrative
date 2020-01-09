package org.narrative.network.customizations.narrative.niches.tribunal.dao;

import org.narrative.common.persistence.NameValuePair;
import org.narrative.common.persistence.OID;
import org.narrative.network.core.area.portfolio.Portfolio;
import org.narrative.network.core.area.user.AreaUserRlm;
import org.narrative.network.customizations.narrative.channels.Channel;
import org.narrative.network.customizations.narrative.niches.referendum.ReferendumType;
import org.narrative.network.customizations.narrative.niches.tribunal.TribunalIssue;
import org.narrative.network.customizations.narrative.niches.tribunal.TribunalIssueStatus;
import org.narrative.network.customizations.narrative.niches.tribunal.TribunalIssueType;
import org.narrative.network.shared.daobase.GlobalDAOImpl;

import java.util.Collection;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: benjamin
 * Date: Mar 24, 2009
 * Time: 11:37:37 AM
 * To change this template use File | Settings | File Templates.
 */
public class TribunalIssueDAO extends GlobalDAOImpl<TribunalIssue, OID> {
    public TribunalIssueDAO() {
        super(TribunalIssue.class);
    }

    public TribunalIssue getOpenForChannelAndType(Channel channel, TribunalIssueType type) {
        return getUniqueBy(new NameValuePair<>(TribunalIssue.FIELD__CHANNEL__NAME, channel), new NameValuePair<>(TribunalIssue.FIELD__TYPE__NAME, type), new NameValuePair<>(TribunalIssue.FIELD__STATUS__NAME, TribunalIssueStatus.OPEN));
    }

    public List<TribunalIssue> getOpenForChannel(Channel channel) {
        return getAllBy(new NameValuePair<>(TribunalIssue.FIELD__CHANNEL__NAME, channel), new NameValuePair<>(TribunalIssue.FIELD__STATUS__NAME, TribunalIssueStatus.OPEN));
    }

    public long getCountOfOpenIssuesByTypeAndStatusPendingResponse(Collection<ReferendumType> referendumTypes, AreaUserRlm voter) {
        return ((Number) getGSession().getNamedQuery("tribunalIssue.getCountOfOpenIssuesByTypeAndStatusPendingResponse")
                .setParameter("voterOid", voter.getOid())
                .setParameterList("referendumTypes", referendumTypes)
                .uniqueResult()).longValue();
    }

    public List<TribunalIssue> getOpenIssuesByTypeAndStatusPendingResponse(Collection<ReferendumType> types, AreaUserRlm voter, int page, int resultsPerPage) {
        return getGSession().getNamedQuery("tribunalIssue.getOpenIssuesByTypeAndStatusPendingResponse")
                .setParameter("voterOid", voter.getOid())
                .setParameterList("referendumTypes", types)
                .setFirstResult(Math.max(0, page - 1) * resultsPerPage)
                .setMaxResults(resultsPerPage)
                .list();
    }

    public List<TribunalIssue> getAllForChannel(Channel channel) {
        return getAllBy(new NameValuePair<>(TribunalIssue.FIELD__CHANNEL__NAME, channel));
    }
}