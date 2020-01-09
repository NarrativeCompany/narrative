import { graphql } from 'react-apollo';
import { FetchResult } from 'apollo-link';
import { nominateCurrentUserMutation } from '../graphql/election/nominateCurrentUserMutation';
import {
  NominateCurrentUserInput,
  NominateCurrentUserMutation,
  NominateCurrentUserMutation_nominateCurrentUser,
  NominateCurrentUserMutationVariables
} from '../../types';
import { resolveExceptionFromApolloError } from '../../utils';

export interface WithNominateCurrentUserProps {
  nominateCurrentUser: (input: NominateCurrentUserInput, electionOid: string) =>
    Promise<NominateCurrentUserMutation_nominateCurrentUser>;
}

export const withNominateCurrentUser =
  graphql<
    {},
    NominateCurrentUserMutation,
    NominateCurrentUserMutationVariables,
    WithNominateCurrentUserProps
  >(nominateCurrentUserMutation, {
    props: ({mutate}) => ({
      nominateCurrentUser: async (input: NominateCurrentUserInput, electionOid: string) => {
        const variables: NominateCurrentUserMutationVariables = {
          input,
          electionOid
        };
        const options = {
          variables,
        };

        if (!mutate) {
          throw new Error ('withNominateCurrentUser: missing mutate');
        }

        return mutate(options)
          .then((response: FetchResult<NominateCurrentUserMutation>) => {
            const electionDetail =
              response &&
              response.data &&
              response.data.nominateCurrentUser;

            if (!electionDetail) {
              throw new Error('withNominateCurrentUser: no return value from mutation');
            }

            return electionDetail;
          }).catch((error) => {
            throw resolveExceptionFromApolloError(error);
          });
      }
    })
  });
