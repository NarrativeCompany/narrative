package org.narrative.network.customizations.narrative.niches.tribunal.dao;

import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.ObjectPair;
import org.narrative.network.core.area.user.AreaUserRlm;
import org.narrative.network.customizations.narrative.niches.referendum.Referendum;
import org.narrative.network.customizations.narrative.niches.tribunal.TribunalIssue;
import org.narrative.network.customizations.narrative.niches.tribunal.TribunalIssueReport;
import org.narrative.network.customizations.narrative.niches.tribunal.TribunalIssueType;
import org.narrative.network.shared.daobase.GlobalDAOImpl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: benjamin
 * Date: Mar 24, 2009
 * Time: 11:37:37 AM
 * To change this template use File | Settings | File Templates.
 */
public class TribunalIssueReportDAO extends GlobalDAOImpl<TribunalIssueReport, OID> {
    public TribunalIssueReportDAO() {
        super(TribunalIssueReport.class);
    }

    public int getCountForIssue(TribunalIssue issue) {
        return ((Number) getGSession().getNamedQuery("tribunalIssueReport.getCountForIssue").setParameter("tribunalIssue", issue).uniqueResult()).intValue();
    }

    public Map<TribunalIssue, Number> getCountByIssue(Collection<Referendum> forReferendums) {
        if (isEmptyOrNull(forReferendums)) {
            return Collections.emptyMap();
        }

        List<ObjectPair<TribunalIssue, Number>> issueAndCount = getGSession()
                .createNamedQuery("tribunalIssueReport.getCountByIssue", (Class<ObjectPair<TribunalIssue,Number>>)(Class)ObjectPair.class)
                .setParameterList("referendums", forReferendums).list();

        return ObjectPair.getAsMap(issueAndCount);
    }

    public List<TribunalIssueReport> getForIssue(TribunalIssue issue, int page, int reportsPerPage) {
        return getGSession().getNamedQuery("tribunalIssueReport.getForIssue").setParameter("tribunalIssue", issue).setMaxResults(reportsPerPage).setFirstResult(Math.max(0, page - 1) * reportsPerPage).list();
    }

    public TribunalIssueReport getMostRecentReportSubmittedByUser(AreaUserRlm areaUserRlm) {
        return (TribunalIssueReport) getGSession().getNamedQuery("tribunalIssueReport.getMostRecentReportSubmittedByUser")
                .setParameter("areaUserRlm", areaUserRlm)
                .setParameterList("issueTypes", TribunalIssueType.APPEAL_TYPES)
                .setMaxResults(1)
                .uniqueResult();
    }
}