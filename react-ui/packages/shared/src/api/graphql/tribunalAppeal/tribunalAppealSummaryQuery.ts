import gql from 'graphql-tag';
import { TribunalIssueDetailFragment } from '../fragments/tribunalIssueDetailFragment';

export const tribunalAppealSummaryQuery = gql`
  query TribunalAppealSummaryQuery ($input: TribunalAppealSummaryInput!) {
    getTribunalAppealSummary (input: $input) 
    @rest (type: "TribunalIssueDetail", path: "/tribunal/appeals/{args.input.tribunalIssueOid}") {
      ...TribunalIssueDetail
    }
  }
  ${TribunalIssueDetailFragment}
`;
