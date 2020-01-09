import { graphql } from 'react-apollo';
import { FetchResult } from 'apollo-link';
import { startFollowingChannelMutation } from '../graphql/niche/startFollowingChannelMutation';
import {
  FollowChannelInput,
  StartFollowingChannelMutation,
  StartFollowingChannelMutation_startFollowingChannel,
  StartFollowingChannelMutationVariables,
} from '../../types';
import { resolveExceptionFromApolloError } from '../../utils';

export interface WithStartFollowingChannelProps {
  startFollowingChannel: (input: FollowChannelInput) => Promise<StartFollowingChannelMutation_startFollowingChannel>;
}

export const withStartFollowingChannel =
  graphql<
    {},
    StartFollowingChannelMutation,
    StartFollowingChannelMutationVariables,
    WithStartFollowingChannelProps
  >(startFollowingChannelMutation, {
    props: ({mutate}) => ({
      startFollowingChannel: async (input: FollowChannelInput) => {
        const variables: StartFollowingChannelMutationVariables = {input};
        const options = {
          variables
        };

        if (!mutate) {
          throw new Error ('withStartFollowingChannel: missing mutate');
        }

        return mutate(options)
          .then((response: FetchResult<StartFollowingChannelMutation>) => {
            const followNicheRes =
              response &&
              response.data &&
              response.data.startFollowingChannel;

            if (!followNicheRes) {
              throw new Error('withStartFollowingChannel: no return value from mutation');
            }

            return followNicheRes;
        }).catch((error) => {
            throw resolveExceptionFromApolloError(error);
          });
      }
    })
  });
