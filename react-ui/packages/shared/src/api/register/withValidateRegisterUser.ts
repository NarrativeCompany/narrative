import { graphql } from 'react-apollo';
import { FetchResult } from 'apollo-link';
import { validateRegisterUserMutation } from '../graphql/register/validateRegisterUserMutation';
import {
  RegisterUserInput,
  ValidateRegisterUserMutation,
  ValidateRegisterUserMutation_validateRegisterUser,
  ValidateRegisterUserMutationVariables
} from '../../types';
import { resolveExceptionFromApolloError } from '../../utils';

export interface WithValidateRegisterUserProps {
  validateRegisterUser: (input: RegisterUserInput) => Promise<ValidateRegisterUserMutation_validateRegisterUser>;
}

export const withValidateRegisterUser =
  graphql<
    {},
    ValidateRegisterUserMutation,
    ValidateRegisterUserMutationVariables,
    WithValidateRegisterUserProps
  >(validateRegisterUserMutation, {
    props: ({mutate}) => ({
      validateRegisterUser: async (input: RegisterUserInput) => {
        const variables: ValidateRegisterUserMutationVariables = {input};
        const options = {
          variables,
        };

        if (!mutate) {
          throw new Error ('withValidateRegisterUser: missing mutate');
        }

        return mutate(options)
          .then((response: FetchResult<ValidateRegisterUserMutation>) => {

            const res = response && response.data && response.data.validateRegisterUser;

            if (!res) {
              throw new Error('withValidateRegisterUser: no return value from mutation');
            }

            return res;
          }).catch((error) => {
            throw resolveExceptionFromApolloError(error);
          });
      }
    })
  });
