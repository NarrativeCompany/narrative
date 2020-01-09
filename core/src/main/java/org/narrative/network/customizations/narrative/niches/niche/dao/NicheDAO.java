package org.narrative.network.customizations.narrative.niches.niche.dao;

import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.ObjectSeptuplet;
import org.narrative.common.persistence.PersistenceUtil;
import org.narrative.network.core.area.base.AreaRlm;
import org.narrative.network.core.area.portfolio.Portfolio;
import org.narrative.network.core.area.user.AreaUserRlm;
import org.narrative.network.core.content.base.ContentType;
import org.narrative.network.core.content.base.SEOObjectDAO;
import org.narrative.network.core.content.base.services.CreateContentTask;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.customizations.narrative.niches.niche.NicheStatus;
import org.narrative.network.customizations.narrative.service.api.model.FollowScrollParamsDTO;
import org.narrative.network.shared.daobase.GlobalDAOImpl;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: martin
 * Date: 12/2/2018
 * Time: 09:20
 */
public class NicheDAO extends GlobalDAOImpl<Niche, OID> implements SEOObjectDAO<Niche> {
    public NicheDAO() {
        super(Niche.class);
    }

    public ScrollableResults getAllOidsAndAreaOids() {
        return getGSession().getNamedQuery("niche.getAllOidsAndAreaOids").scroll(ScrollMode.FORWARD_ONLY);
    }

    public List<ObjectSeptuplet<OID, OID, String, String, Timestamp, NicheStatus, OID>> getIndexRecordChunked(OID lastOid, int chunkSize) {
        return getGSession().getNamedQuery("niche.getIndexRecordChunked").setParameter("lastOid", lastOid).setMaxResults(chunkSize).list();
    }

    public List<ObjectSeptuplet<OID, OID, String, String, Timestamp, NicheStatus, OID>> getIndexRecordForAreaRlm(AreaRlm areaRlm) {
        return getGSession().getNamedQuery("niche.getIndexRecordForAreaRlm").setParameter("areaRlm", areaRlm).list();
    }

    public Niche getForReservedName(Portfolio portfolio, String reservedName) {
        return (Niche) getGSession().getNamedQuery("niche.getForReservedName")
                .setParameter("portfolio", portfolio)
                .setParameter("reservedName", reservedName)
                .setCacheable(true)
                .uniqueResult();
    }

    public List<Niche> getReservedRejectedNichesOlderThan(Portfolio portfolio, Instant olderThan) {
        return getGSession().getNamedQuery("niche.getReservedRejectedNichesOlderThan")
                .setParameter("portfolio", portfolio)
                .setParameter("lastStatusChangeBefore", new Timestamp(olderThan.toEpochMilli()))
                .setParameter("rejectedStatus", NicheStatus.REJECTED)
                .list();
    }

    public String getAvailablePrettyUrlString(Portfolio portfolio, String name) {
        return CreateContentTask.getPrettyUrlStringValue(this, portfolio.getAreaRlm(), portfolio, null, name);
    }

    @Override
    public Niche getForPrettyURLString(AreaRlm areaRlm, Portfolio portfolio, ContentType contentType, String prettyUrlString) {
        assert contentType == null : "Should never supply ContentType when getting Niche for prettyUrlString! contentType/" + contentType;
        return getForPrettyURLString(portfolio, prettyUrlString);
    }

    public Niche getForPrettyURLString(Portfolio portfolio, String prettyUrlString) {
        return (Niche) getGSession().getNamedQuery("niche.getForPrettyUrlString").setParameter("portfolio", portfolio).setParameter("prettyUrlString", prettyUrlString).setCacheable(true).uniqueResult();
    }

    public Niche getMostRecentNicheSuggestedByUser(AreaUserRlm suggester) {
        return (Niche) getGSession().getNamedQuery("niche.getMostRecentNicheSuggestedByUser").setParameter("suggester", suggester)
                // jw: while the user can have more than one niche suggested, we are only interested in the most recent one.
                .setMaxResults(1).uniqueResult();
    }

    public long getCountOfNicheOwners() {
        Number value = ((Number)getGSession().getNamedQuery("niche.getCountOfNicheOwners")
                .setParameter("activeStatus", NicheStatus.ACTIVE)
                .uniqueResult());
        return value != null ? value.longValue() : 0L;
    }

    public long getCountOfNichesByStatus(Collection<NicheStatus> statuses) {
        Number value = ((Number)getGSession().getNamedQuery("niche.getCountOfNichesByStatus")
                .setParameterList("statuses", statuses)
                .uniqueResult());
        return value != null ? value.longValue() : 0L;
    }

    public List<Niche> getNichesFollowedByUser(User follower, FollowScrollParamsDTO params, int maxResults) {
        assert exists(follower) : "Expect to get a user at this point!";

        return getGSession().createNamedQuery("niche.getNichesFollowedByUser", Niche.class)
                .setParameter("follower", follower)
                .setParameter("lastName", params == null ? null : params.getLastItemName())
                .setParameter("lastOid", params == null ? null : params.getLastItemOid())
                .setMaxResults(maxResults)
                .list();
    }

    public List<Niche> getActiveNichesByName(String name, int maxResults){
        return getGSession().createNamedQuery("niche.getActiveNichesByName", Niche.class)
                .setParameter("name", PersistenceUtil.MySQLUtils.getStringAfterEscapingWildcardCharsForHqlLikePattern(name) + "%")
                .setParameter("activeStatus", NicheStatus.ACTIVE)
                .setMaxResults(maxResults)
                .list();

    }
}