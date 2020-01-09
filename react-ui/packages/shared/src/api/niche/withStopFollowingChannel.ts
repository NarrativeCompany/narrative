import { graphql } from 'react-apollo';
import { FetchResult } from 'apollo-link';
import { stopFollowingChannelMutation } from '../graphql/niche/stopFollowingChannelMutation';
import {
  FollowChannelInput,
  StopFollowingChannelMutation,
  StopFollowingChannelMutation_stopFollowingChannel,
  StopFollowingChannelMutationVariables
} from '../../types';
import { resolveExceptionFromApolloError } from '../../utils';

export interface WithStopFollowingChannelProps {
  stopFollowingChannel: (input: FollowChannelInput) => Promise<StopFollowingChannelMutation_stopFollowingChannel>;
}

export const withStopFollowingChannel =
  graphql<
    {},
    StopFollowingChannelMutation,
    StopFollowingChannelMutationVariables,
    WithStopFollowingChannelProps
  >(stopFollowingChannelMutation, {
    props: ({mutate}) => ({
      stopFollowingChannel: async (input: FollowChannelInput) => {
        const variables: StopFollowingChannelMutationVariables = {input};
        const options = {
          variables
        };

        if (!mutate) {
          throw new Error ('withStopFollowingChannel: missing mutate');
        }

        return mutate(options)
          .then((response: FetchResult<StopFollowingChannelMutation>) => {
            const followNicheRes =
              response &&
              response.data &&
              response.data.stopFollowingChannel;

            if (!followNicheRes) {
              throw new Error('withStopFollowingChannel: no return value from mutation');
            }

            return followNicheRes;
          }).catch((error) => {
            throw resolveExceptionFromApolloError(error);
          });
      }
    })
  });
