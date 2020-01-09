import { graphql } from 'react-apollo';
import { FetchResult } from 'apollo-link';
import { authStateMutation } from '../graphql/state/authStateMutation';
import { AuthStateInput, AuthStateMutation } from '../../types';
import { resolveExceptionFromApolloError } from '../../utils';

export interface WithUpdateAuthStateProps {
  updateAuthenticationState: (input: AuthStateInput) => Promise<boolean>;
}

export const withUpdateAuthState = graphql(authStateMutation, {
  props: ({mutate}) => ({
    updateAuthenticationState: async (input: AuthStateInput) => {
      const variables = {input};
      const options = {
        variables
      };

      if (!mutate) {
        throw new Error('withUpdateAuthState: missing mutate');
      }

      return mutate(options)
        .then((response: FetchResult<AuthStateMutation>) => {
          return response &&
            response.data &&
            response.data.updateAuthenticationState;
        }).catch((error) => {
          throw resolveExceptionFromApolloError(error);
        });
    }
  })
});
