package org.narrative.network.customizations.narrative.niches.tribunal.services;

import org.narrative.network.customizations.narrative.niches.tribunal.TribunalIssueReport;
import org.narrative.network.customizations.narrative.service.impl.tribunal.SendTribunalEmailTaskBase;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 3/6/18
 * Time: 10:36 AM
 */
public class SendNewTribunalIssueReportEmail extends SendTribunalEmailTaskBase {
    private TribunalIssueReport report;
    private final boolean forNewIssue;

    public SendNewTribunalIssueReportEmail(TribunalIssueReport report, boolean forNewIssue) {
        this.report = report;
        this.forNewIssue = forNewIssue;
    }

    public TribunalIssueReport getReport() {
        return report;
    }

    public boolean isForNewIssue() {
        return forNewIssue;
    }
}
