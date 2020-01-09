import { graphql } from 'react-apollo';
import { FetchResult } from 'apollo-link';
import { validatePostMutation } from '../graphql/post/validatePostMutation';
import { PostTextInput, ValidatePostMutation, ValidatePostMutationVariables, } from '../../types';
import { resolveExceptionFromApolloError } from '../../utils';

export interface WithValidatePostProps {
  validatePost: (input: PostTextInput) => Promise<ValidatePostMutation>;
}

export const withValidatePost =
  graphql<
    {},
    ValidatePostMutation,
    ValidatePostMutationVariables,
    WithValidatePostProps
  >(validatePostMutation, {
    props: ({mutate}) => ({
      validatePost: async (input: PostTextInput) => {
        const variables: ValidatePostMutationVariables = {input};
        const options = {
          variables,
        };

        if (!mutate) {
          throw new Error ('withValidatePost: missing mutate');
        }

        return mutate(options)
          .then((response: FetchResult<ValidatePostMutation>) => {

            const res = response && response.data;

            if (!res) {
              throw new Error('withValidatePost: no return value from mutation');
            }

            return res;
          }).catch((error) => {
            throw resolveExceptionFromApolloError(error);
          });
      }
    })
  });
