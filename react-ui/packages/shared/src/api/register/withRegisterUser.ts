import { graphql } from 'react-apollo';
import { FetchResult } from 'apollo-link';
import {
  RegisterUserInput,
  RegisterUserMutation,
  RegisterUserMutation_registerUser,
  RegisterUserMutationVariables
} from '../../types';
import { registerUserMutation } from '../graphql/register/registerUserMutation';
import { resolveExceptionFromApolloError } from '../../utils';

export interface WithRegisterUserProps {
  registerUser: (input: RegisterUserInput) => Promise<RegisterUserMutation_registerUser>;
}

export const withRegisterUser = graphql(registerUserMutation, {
  props: ({mutate}) => ({
    registerUser: async (input: RegisterUserInput) => {
      const variables: RegisterUserMutationVariables = {input};
      const options = {
        variables
      };

      if (!mutate) {
        throw new Error ('withRegisterUser: missing mutate');
      }

      return mutate(options)
        .then((response: FetchResult<RegisterUserMutation>) => {
          const registerUser =
            response &&
            response.data &&
            response.data.registerUser;

          if (!registerUser) {
            throw new Error('withRegisterUser: no return value from register mutation');
          }

          return registerUser;
        }).catch((error) => {
          throw resolveExceptionFromApolloError(error);
        });
    }
  })
});
