import gql from 'graphql-tag';
import { TribunalIssueFragment } from '../fragments/tribunalIssueFragment';
import { PageInfoFragment } from '../fragments/pageInfoFragment';

export const allNichesUnderTribunalReviewQuery = gql`
  query AllNichesUnderTribunalReviewQuery ($size: Int, $page: Int) {
    getAllNichesUnderTribunalReview (size: $size, page: $page) 
    @rest(type: "TribunalAppealsListPayload", path: "/tribunal/niches-under-review?{args}") {
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
