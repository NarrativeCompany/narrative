package org.narrative.network.customizations.narrative.niches.tribunal;

/**
 * Date: 6/4/18
 * Time: 4:16 PM
 *
 * @author martin
 */
public enum LastTribunalIssueReportType {
    FIRST {
        @Override
        public TribunalIssueReport getForTribunalIssue(TribunalIssue issue) {
            return issue.getLastReport();
        }

        @Override
        public void setForTribunalIssue(TribunalIssue issue, TribunalIssueReport report) {
            issue.setLastReport(report);
        }
    },
    SECOND {
        @Override
        public TribunalIssueReport getForTribunalIssue(TribunalIssue issue) {
            return issue.getLastReport2();
        }

        @Override
        public void setForTribunalIssue(TribunalIssue issue, TribunalIssueReport report) {
            issue.setLastReport2(report);
        }
    },
    THIRD {
        @Override
        public TribunalIssueReport getForTribunalIssue(TribunalIssue issue) {
            return issue.getLastReport3();
        }

        @Override
        public void setForTribunalIssue(TribunalIssue issue, TribunalIssueReport report) {
            issue.setLastReport3(report);
        }
    };

    public abstract TribunalIssueReport getForTribunalIssue(TribunalIssue tribunalIssue);

    public abstract void setForTribunalIssue(TribunalIssue tribunalIssue, TribunalIssueReport report);

}
