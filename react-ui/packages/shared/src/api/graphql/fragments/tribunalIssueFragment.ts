import gql from 'graphql-tag';
import { TribunalIssueReportFragment } from './tribunalIssueReportFragment';
import { ReferendumFragment } from './referendumFragment';
import { NicheEditDetailFragment } from './nicheEditDetailFragment';

export const TribunalIssueFragment = gql`
  fragment TribunalIssue on TribunalIssue {
    oid
    type
    status
    creationDatetime
    referendum @type(name: "Referendum") {
      ...Referendum
    }
    lastReport @type(name: "TribunalIssueReport") {
      ...TribunalIssueReport
    }
    nicheEditDetail @type(name: "NicheEditDetail") {
      ...NicheEditDetail
    }
  }
  ${ReferendumFragment}
  ${TribunalIssueReportFragment}
  ${NicheEditDetailFragment}
`;
