import { graphql } from 'react-apollo';
import { FetchResult } from 'apollo-link';
import {
  EnableTwoFactorAuthMutation,
  EnableTwoFactorAuthMutation_enableTwoFactorAuth,
  EnableTwoFactorAuthMutationVariables
} from '../../types';
import { enableTwoFactorAuthMutation } from '../graphql/login/enableTwoFactorAuthMutation';
import { resolveExceptionFromApolloError } from '../../utils';

export interface WithEnableTwoFactorAuthProps {
  enableTwoFactorAuth: (input: EnableTwoFactorAuthMutationVariables) =>
    Promise<EnableTwoFactorAuthMutation_enableTwoFactorAuth>;
}

export const withEnableTwoFactorAuth = graphql(enableTwoFactorAuthMutation, {
  props: ({mutate}) => ({
    enableTwoFactorAuth: async (input: EnableTwoFactorAuthMutationVariables) => {
      const variables: EnableTwoFactorAuthMutationVariables = input;
      const options = {
        variables
      };

      if (!mutate) {
        throw new Error ('WithEnableTwoFactorAuthProps: missing mutate');
      }

      return mutate(options)
        .then((response: FetchResult<EnableTwoFactorAuthMutation>) => {
          const res =
            response &&
            response.data &&
            response.data.enableTwoFactorAuth;

          if (!res) {
            throw new Error('withEnableTwoFactorAuthProps: no return value from mutation');
          }

          return res;
        }).catch((error) => {
          throw resolveExceptionFromApolloError(error);
        });
    }
  })
});
