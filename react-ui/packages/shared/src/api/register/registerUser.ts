import { apolloClient } from '../apiGraphqlUtil';
import { MutationOptions } from 'apollo-client/core/watchQueryOptions';
import { FetchResult } from 'apollo-link';
import { RegisterUserMutation_registerUser, RegisterUserMutationVariables } from '../../types';
import { registerUserMutation } from '../graphql/register/registerUserMutation';

export async function registerUser (input: RegisterUserMutationVariables):
  Promise<FetchResult<RegisterUserMutation_registerUser>> {
  const variables: RegisterUserMutationVariables = input;

  const options: MutationOptions = {
    variables,
    mutation: registerUserMutation
  };

  return apolloClient.mutate<RegisterUserMutation_registerUser>(options);
}
