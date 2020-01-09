import gql from 'graphql-tag';
import { CommentFragment } from '../fragments/commentFragment';
import { PageInfoFragment } from '../fragments/pageInfoFragment';

// tslint:disable: max-line-length
export const commentsQuery = gql`
  query CommentsQuery ($queryFields: CompositionConsumerQueryFieldsInput!, $pageInput: GetCommentsPageQueryInput!) {
    
    getComments (queryFields: $queryFields, pageInput: $pageInput) 
    @rest(
      type: "CommentsPageData", 
      path: "/comments/{args.queryFields.consumerType}/{args.queryFields.consumerOid}/?{args.pageInput}",
    ) {
      items @type(name: "Comment") {
        ...Comment
      }
      info @type(name: "PageInfo") {
        ...PageInfo
      }
      buriedCommentCount
      includeBuried
    }
  }
  ${CommentFragment}
  ${PageInfoFragment}
`;
