import { graphql } from 'react-apollo';
import { FetchResult } from 'apollo-link';
import {
  UpdatePasswordMutation,
  UpdatePasswordMutation_updatePassword,
  UpdatePasswordMutationVariables
} from '../../types';
import { updatePasswordMutation } from '../graphql/user/updatePasswordMutation';
import { resolveExceptionFromApolloError } from '../../utils';

export interface WithUpdatePasswordProps {
  updatePassword: (input: UpdatePasswordMutationVariables) =>
    Promise<UpdatePasswordMutation_updatePassword>;
}

export const withUpdatePassword = graphql(updatePasswordMutation, {
  props: ({mutate}) => ({
    updatePassword: async (input: UpdatePasswordMutationVariables) => {
      const variables: UpdatePasswordMutationVariables = input;
      const options = {
        variables
      };

      if (!mutate) {
        throw new Error ('withUpdatePassword: missing mutate');
      }

      return mutate(options)
        .then((response: FetchResult<UpdatePasswordMutation>) => {
          const res =
            response &&
            response.data &&
            response.data.updatePassword;

          if (!res) {
            throw new Error('withUpdatePassword: no return value from mutation');
          }

          return res;
        }).catch((error) => {
          throw resolveExceptionFromApolloError(error);
        });
    }
  })
});
