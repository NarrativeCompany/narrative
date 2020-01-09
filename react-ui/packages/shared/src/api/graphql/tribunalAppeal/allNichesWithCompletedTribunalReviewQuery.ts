import gql from 'graphql-tag';
import { TribunalIssueFragment } from '../fragments/tribunalIssueFragment';
import { PageInfoFragment } from '../fragments/pageInfoFragment';

export const allNichesWithCompletedTribunalReviewQuery = gql`
  query AllNichesWithCompletedTribunalReviewQuery ($size: Int, $page: Int) {
    getAllNichesWithCompletedTribunalReview (size: $size, page: $page)
    @rest(type: "TribunalAppealsListPayload", path: "/tribunal/niches-completed-review?{args}") {
      items @type(name: "TribunalIssue") {
        ...TribunalIssue
      }
      info @type(name: "PageInfo") {
        ...PageInfo
      }
    }
  }
  ${TribunalIssueFragment}
  ${PageInfoFragment}
`;
