import { graphql } from 'react-apollo';
import { FetchResult } from 'apollo-link';
import { submitPostMutation } from '../graphql/post/submitPostMutation';
import {
  PostInput,
  SubmitPostMutation,
  SubmitPostMutation_submitPost,
  SubmitPostMutationVariables,
} from '../../types';
import { resolveExceptionFromApolloError } from '../../utils';

export interface WithSubmitPostProps {
  submitPost: (input: PostInput) => Promise<SubmitPostMutation_submitPost>;
}

export const withSubmitPost =
  graphql<
    {},
    SubmitPostMutation,
    SubmitPostMutationVariables,
    WithSubmitPostProps
  >(submitPostMutation, {
    props: ({mutate}) => ({
      submitPost: async (input: PostInput) => {
        const variables: SubmitPostMutationVariables = {input};
        const options = {
          variables,
        };

        if (!mutate) {
          throw new Error ('withSubmitPost: missing mutate');
        }

        return mutate(options)
          .then((response: FetchResult<SubmitPostMutation>) => {

            const editPostDetail =
              response &&
              response.data &&
              response.data.submitPost;

            if (!editPostDetail) {
              throw new Error('withSubmitPost: no return value from mutation');
            }

            return editPostDetail;
          }).catch((error) => {
            throw resolveExceptionFromApolloError(error);
          });
      }
    })
  });
