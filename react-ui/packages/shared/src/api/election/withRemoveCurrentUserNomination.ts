import { graphql } from 'react-apollo';
import { FetchResult } from 'apollo-link';
import { removeCurrentUserNominationMutation } from '../graphql/election/removeCurrentUserNominationMutation';
import {
  RemoveCurrentUserNominationMutation,
  RemoveCurrentUserNominationMutation_removeCurrentUserNomination,
  RemoveCurrentUserNominationMutationVariables
} from '../../types';
import { resolveExceptionFromApolloError } from '../../utils';

export interface WithRemoveCurrentUserNominationProps {
  removeCurrentUserNomination: (electionOid: string) =>
    Promise<RemoveCurrentUserNominationMutation_removeCurrentUserNomination>;
}

export const withRemoveCurrentUserNominee =
  graphql<
    {},
    RemoveCurrentUserNominationMutation,
    RemoveCurrentUserNominationMutationVariables,
    WithRemoveCurrentUserNominationProps
  >(removeCurrentUserNominationMutation, {
    props: ({mutate}) => ({
      removeCurrentUserNomination: async (electionOid: string) => {
        const variables: RemoveCurrentUserNominationMutationVariables = {
          electionOid
        };
        const options = {
          variables,
        };

        if (!mutate) {
          throw new Error ('withRemoveCurrentUserNominee: missing mutate');
        }

        return mutate(options)
          .then((response: FetchResult<RemoveCurrentUserNominationMutation>) => {
            const electionDetail =
              response &&
              response.data &&
              response.data.removeCurrentUserNomination;

            if (!electionDetail) {
              throw new Error('withRemoveCurrentUserNominee: no return value from mutation');
            }

            return electionDetail;
          }).catch((error) => {
            throw resolveExceptionFromApolloError(error);
          });
      }
    })
  });
