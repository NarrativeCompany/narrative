import { graphql } from 'react-apollo';
import { editCommentMutation } from '../graphql/comment/editCommentMutation';
import {
  CommentInput,
  CommentQueryFieldsInput,
  EditCommentMutation,
  EditCommentMutation_editComment,
  EditCommentMutationVariables
} from '../../types';
import { mutationResolver } from '../../utils';
import { WithCommentPostingProps, WithCommentQueryProps } from './commentQueryConstants';

const functionName = 'editComment';

type ParentProps =
  WithCommentPostingProps &
  WithCommentQueryProps;

export interface WithEditCommentProps {
  [functionName]: (input: CommentInput, queryField: CommentQueryFieldsInput) =>
    Promise<EditCommentMutation_editComment>;
}

export const withEditComment =
  graphql<
    ParentProps,
    EditCommentMutation,
    EditCommentMutationVariables,
    WithEditCommentProps
  >(editCommentMutation, {
    props: ({mutate}) => ({
      [functionName]: async ( input: CommentInput, queryFields: CommentQueryFieldsInput) => {
        return await mutationResolver<EditCommentMutation>(mutate, {
          variables: { input, queryFields }
        }, functionName);
      }
    })
  });
