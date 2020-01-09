import gql from 'graphql-tag';
import { TribunalIssueFragment } from './tribunalIssueFragment';
import { TribunalIssueReportFragment } from './tribunalIssueReportFragment';

export const TribunalIssueDetailFragment = gql`
  fragment TribunalIssueDetail on TribunalIssueDetail {
    tribunalIssue @type(name: "TribunalIssue") {
      ...TribunalIssue
    }
    tribunalIssueReports @type(name: "TribunalIssueReport") {
      ...TribunalIssueReport
    }
  }
  ${TribunalIssueFragment}
  ${TribunalIssueReportFragment}
`;
