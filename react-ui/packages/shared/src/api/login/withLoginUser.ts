import { graphql } from 'react-apollo';
import { FetchResult } from 'apollo-link';
import { LoginMutation, LoginMutation_login, LoginMutationVariables } from '../../types';
import { loginUserMutation } from '../graphql/login/loginUserMutation';
import { resolveExceptionFromApolloError } from '../../utils/errorUtils';

export interface WithLoginUserProps {
  loginUser: (input: LoginMutationVariables) => Promise<LoginMutation_login>;
}

export const withLoginUser = graphql(loginUserMutation, {
  props: ({mutate}) => ({
    loginUser: async (input: LoginMutationVariables) => {
      const variables: LoginMutationVariables = input;
      const options = {
        variables,
        mutation: loginUserMutation
      };

      if (!mutate) {
        throw new Error ('withLoginUser: missing mutate');
      }

      return mutate(options)
        .then((response: FetchResult<LoginMutation>) => {
          const login =
            response &&
            response.data &&
            response.data.login;

          if (!login) {
            throw new Error('withLoginUser: no return value from login mutation');
          }

          return login;
        }).catch((error) => {
           throw resolveExceptionFromApolloError(error);
        });
    }
  })
});
