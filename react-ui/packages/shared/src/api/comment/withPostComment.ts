import { graphql } from 'react-apollo';
import { postCommentMutation } from '../graphql/comment/postCommentMutation';
import {
  CommentInput,
  CompositionConsumerQueryFieldsInput,
  PostCommentMutation,
  PostCommentMutation_postComment,
  PostCommentMutationVariables
} from '../../types';
import { mutationResolver } from '../../utils';
import { WithCommentPostingProps, WithCompositionConsumerFields } from './commentQueryConstants';

const functionName = 'postComment';

type ParentProps = WithCommentPostingProps &
  WithCompositionConsumerFields;

export interface WithPostCommentProps {
  [functionName]: (input: CommentInput, queryFields: CompositionConsumerQueryFieldsInput) =>
    Promise<PostCommentMutation_postComment>;
}

export const withPostComment =
  graphql<
    ParentProps,
    PostCommentMutation,
    PostCommentMutationVariables,
    WithPostCommentProps
  >(postCommentMutation, {
    props: ({mutate}) => ({
      [functionName]: async (input: CommentInput, queryFields: CompositionConsumerQueryFieldsInput) => {
        return await mutationResolver<PostCommentMutation>(mutate, {
          variables: { input, queryFields }
        }, functionName);
      }
    })
  });
