import gql from 'graphql-tag';
import { UserFragment } from './userFragment';

export const TribunalIssueReportFragment = gql`
  fragment TribunalIssueReport on TribunalIssueReport {
    oid
    reporter @type(name: "User") {
      ...User
    }
    comments
    creationDatetime
  }
  ${UserFragment}
`;
