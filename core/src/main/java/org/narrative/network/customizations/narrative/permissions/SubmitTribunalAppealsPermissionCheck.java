package org.narrative.network.customizations.narrative.permissions;

import org.narrative.common.util.IPDateUtil;
import org.narrative.network.core.security.area.base.AreaRole;
import org.narrative.network.customizations.narrative.niches.tribunal.TribunalIssueReport;
import org.narrative.network.customizations.narrative.service.api.model.permissions.SubmitTribunalAppealsRevokeReason;
import org.narrative.network.shared.util.NetworkDateUtils;

import java.sql.Timestamp;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: 2019-02-10
 * Time: 16:21
 *
 * @author brian
 */
public class SubmitTribunalAppealsPermissionCheck implements NarrativePermissionCheck {
    @Override
    public void checkRight(AreaRole areaRole) {
        // check to make sure the user hasn't submitted an appeal within the past 24 hours
        TribunalIssueReport report = TribunalIssueReport.dao().getMostRecentReportSubmittedByUser(areaRole.getAreaUserRlm());
        if (exists(report)) {
            Timestamp reportAllowedDate = new Timestamp(report.getCreationDatetime().getTime() + (TribunalIssueReport.HOURS_BETWEEN_APPEALS * IPDateUtil.HOUR_IN_MS));
            if (reportAllowedDate.after(now())) {
                // if the user is high rep or tribunal, then allow unlimited appeals
                if(!areaRole.getUser().isCanParticipateInTribunalIssues() && !areaRole.getUser().getReputation().getLevel().isHigh()) {
                    throw new NarrativePermissionRevokedError(wordlet("managedNarrativeCircleType.accessError.title.submitTribunalAppeal"), wordlet("tribunalIssue.mustWaitToReport", formatNumber(TribunalIssueReport.HOURS_BETWEEN_APPEALS), NetworkDateUtils.dateFormatShortDatetimeNoPrettyTime(reportAllowedDate, null, false)), SubmitTribunalAppealsRevokeReason.REPORTED_IN_LAST_24_HOURS, reportAllowedDate.toInstant());
                }
            }
        }
    }
}
