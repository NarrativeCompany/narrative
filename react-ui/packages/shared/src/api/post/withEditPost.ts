import { graphql } from 'react-apollo';
import { FetchResult } from 'apollo-link';
import { editPostMutation } from '../graphql/post/editPostMutation';
import { EditPostMutation, EditPostMutation_editPost, EditPostMutationVariables, PostInput, } from '../../types';
import { resolveExceptionFromApolloError } from '../../utils';

export interface WithEditPostProps {
  editPost: (input: PostInput, postOid: string) => Promise<EditPostMutation_editPost>;
}

export const withEditPost =
  graphql<
    {},
    EditPostMutation,
    EditPostMutationVariables,
    WithEditPostProps
  >(editPostMutation, {
    props: ({mutate}) => ({
      editPost: async (input: PostInput, postOid: string) => {
        const variables: EditPostMutationVariables = {input, postOid};
        const options = {
          variables,
        };

        if (!mutate) {
          throw new Error ('withEditPost: missing mutate');
        }

        return mutate(options)
          .then((response: FetchResult<EditPostMutation>) => {

            const editPostDetail =
              response &&
              response.data &&
              response.data.editPost;

            if (!editPostDetail) {
              throw new Error('withEditPost: no return value from mutation');
            }

            return editPostDetail;
          }).catch((error) => {
            throw resolveExceptionFromApolloError(error);
          });
      }
    })
  });
