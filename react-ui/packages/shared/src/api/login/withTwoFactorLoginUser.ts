import { graphql } from 'react-apollo';
import { FetchResult } from 'apollo-link';
import {
  TwoFactorLoginMutation,
  TwoFactorLoginMutation_twoFactorLogin,
  TwoFactorLoginMutationVariables
} from '../../types';
import { twoFactorLoginUserMutation } from '../graphql/login/twoFactorLoginUserMutation';
import { resolveExceptionFromApolloError } from '../../utils';

export interface WithTwoFactorLoginProps {
  twoFactorLogin: (input: TwoFactorLoginMutationVariables) => Promise<TwoFactorLoginMutation_twoFactorLogin>;
}

export const withTwoFactorLoginUser = graphql(twoFactorLoginUserMutation, {
  props: ({mutate}) => ({
    twoFactorLogin: async (input: TwoFactorLoginMutationVariables) => {
      const variables: TwoFactorLoginMutationVariables = input;
      const options = {
        variables,
        mutation: twoFactorLoginUserMutation
      };

      if (!mutate) {
        throw new Error ('withTwoFactorLoginUser: missing mutate');
      }

      return mutate(options)
        .then((response: FetchResult<TwoFactorLoginMutation>) => {
          const tfLogin =
            response &&
            response.data &&
            response.data.twoFactorLogin;

          if (!tfLogin) {
            throw new Error('withTwoFactorLoginUser: no return value from two factor authentication mutation');
          }

          return tfLogin;
        }).catch((error) => {
          throw resolveExceptionFromApolloError(error);
        });
    }
  })
});
