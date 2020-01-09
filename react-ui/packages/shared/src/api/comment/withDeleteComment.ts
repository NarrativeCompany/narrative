import { graphql } from 'react-apollo';
import { deleteCommentMutation } from '../graphql/comment/deleteCommentMutation';
import {
  CommentQueryFieldsInput,
  DeleteCommentMutation,
  DeleteCommentMutation_deleteComment,
  DeleteCommentMutationVariables
} from '../../types';
import { mutationResolver } from '../../utils';
import { COMMENT_PAGE_SIZE } from './withComments';
import { getCompositionConsumerFields, WithCompositionConsumerFields } from './commentQueryConstants';
import { commentsQuery } from '../graphql/comment/commentsQuery';

const functionName = 'deleteComment';

interface ParentProps extends WithCompositionConsumerFields {
  // jw: this drives me insane! we need to include currentPage because of the refetchQueries
  currentPage: number;
}

export interface WithDeleteCommentProps {
  [functionName]: (input: CommentQueryFieldsInput) =>
    Promise<DeleteCommentMutation_deleteComment>;
}

export const withDeleteComment =
  graphql<
    ParentProps,
    DeleteCommentMutation,
    DeleteCommentMutationVariables,
    WithDeleteCommentProps
  >(deleteCommentMutation, {
    props: ({mutate, ownProps}) => ({
      [functionName]: async (input: CommentQueryFieldsInput) => {
        return await mutationResolver<DeleteCommentMutation>(mutate, {
          variables: { input },
          refetchQueries: [{
            query: commentsQuery,
            variables: {
              queryFields: getCompositionConsumerFields(ownProps),
              pageInput: {
                size: COMMENT_PAGE_SIZE,
                // jw: the server uses indexes, so lets translate from our 1 based value
                page: ownProps.currentPage - 1
              }
            }
          }]
        }, functionName);
      }
    })
  });
