import gql from 'graphql-tag';
import { TribunalIssueFragment } from '../fragments/tribunalIssueFragment';
import { PageInfoFragment } from '../fragments/pageInfoFragment';

export const allMyQueueTribunalReviewQuery = gql`
  query AllMyQueueTribunalReviewQuery ($size: Int, $page: Int) {
    getAllMyQueueTribunalReview (size: $size, page: $page) 
    @rest(type: "TribunalAppealsListPayload", path: "/tribunal/my-queue?{args}") {
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
