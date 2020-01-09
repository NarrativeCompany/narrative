import { graphql } from 'react-apollo';
import { DisableTwoFactorAuthMutationVariables } from '../../types';
import { disableTwoFactorAuthMutation } from '../graphql/login/disableTwoFactorAuthMutation';
import { resolveExceptionFromApolloError } from '../../utils';

export interface WithDisableTwoFactorAuthProps {
  disableTwoFactorAuth: (input: DisableTwoFactorAuthMutationVariables) => Promise<boolean>;
}

export const withDisableTwoFactorAuth = graphql(disableTwoFactorAuthMutation, {
  props: ({mutate}) => ({
    disableTwoFactorAuth: async (input: DisableTwoFactorAuthMutationVariables) => {
      const variables: DisableTwoFactorAuthMutationVariables = input;
      const options = {
        variables
      };

      if (!mutate) {
        throw new Error ('WithDisableTwoFactorAuthProps: missing mutate');
      }

      return mutate(options)
        .then(() => {
          return true;
        }).catch((error) => {
          throw resolveExceptionFromApolloError(error);
        });
    }
  })
});
