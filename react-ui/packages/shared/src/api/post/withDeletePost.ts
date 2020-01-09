import { graphql } from 'react-apollo';
import { FetchResult } from 'apollo-link';
import { deletePostMutation } from '../graphql/post/deletePostMutation';
import { DeletePostMutation, DeletePostMutationVariables } from '../../types';
import { resolveExceptionFromApolloError } from '../../utils';

export interface WithDeletePostProps {
  deletePost: (postOid: string) => Promise<DeletePostMutation>;
}

export const withDeletePost =
  graphql<
    {},
    DeletePostMutation,
    DeletePostMutationVariables,
    WithDeletePostProps
  >(deletePostMutation, {
    props: ({mutate}) => ({
      deletePost: async (postOid: string) => {
        const variables: DeletePostMutationVariables = {postOid};
        const options = { variables };

        if (!mutate) {
          throw new Error ('withDeletePost: missing mutate');
        }

        return mutate(options)
          .then((response: FetchResult<DeletePostMutation>) => {

            const res = response && response.data;

            if (!res) {
              throw new Error('withDeletePost: no return value from mutation');
            }

            return res;
          }).catch((error) => {
            throw resolveExceptionFromApolloError(error);
          });
      }
    })
  });
