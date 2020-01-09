import { graphql } from 'react-apollo';
import { FetchResult } from 'apollo-link';
import { voteOnReferendumMutation } from '../graphql/referendum/voteOnReferendumMutation';
import {
  VoteOnReferendumInput,
  VoteOnReferendumMutation,
  VoteOnReferendumMutation_voteOnReferendum,
  VoteOnReferendumMutationVariables
} from '../../types';
import { resolveExceptionFromApolloError } from '../../utils';

export interface WithVoteOnReferendumProps {
  voteOnReferendum: (input: VoteOnReferendumInput, referendumId: string) =>
    Promise<VoteOnReferendumMutation_voteOnReferendum>;
}

export const withVoteOnReferendum =
  graphql<
    {},
    VoteOnReferendumMutation,
    VoteOnReferendumMutationVariables,
    WithVoteOnReferendumProps
  >(voteOnReferendumMutation, {
    props: ({mutate}) => ({
      voteOnReferendum: async (input: VoteOnReferendumInput, referendumId: string) => {
        const variables: VoteOnReferendumMutationVariables = {
          input,
          referendumId
        };
        const options = {
          variables
        };

        if (!mutate) {
          throw new Error('withVoteOnReferendum: missing mutate!');
        }

        return mutate(options)
          .then((response: FetchResult<VoteOnReferendumMutation>) => {
            const referendum =
              response &&
              response.data &&
              response.data.voteOnReferendum;

            if (!referendum) {
              throw new Error('withLoginUser: no return value from login mutation');
            }

            return referendum;
          }).catch((error) => {
            throw resolveExceptionFromApolloError(error);
          });
      }
    })
  });
