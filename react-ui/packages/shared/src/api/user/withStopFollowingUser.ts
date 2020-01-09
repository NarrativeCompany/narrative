import { graphql } from 'react-apollo';
import { stopFollowingUserMutation } from '../graphql/user/stopFollowingUserMutation';
import {
  StopFollowingUserMutation_stopFollowingUser,
  StopFollowingUserMutation,
  StopFollowingUserMutationVariables,
} from '../../types';
import { mutationResolver } from '../../utils';

const functionName = 'stopFollowingUser';

export interface WithStopFollowingUserProps {
  [functionName]: (userOid: string) => Promise<StopFollowingUserMutation_stopFollowingUser>;
}

export const withStopFollowingUser =
  graphql<
    {},
    StopFollowingUserMutation,
    StopFollowingUserMutationVariables,
    WithStopFollowingUserProps
  >(stopFollowingUserMutation, {
    props: ({mutate}) => ({
      [functionName]: async (userOid: string) => {
        return await mutationResolver<StopFollowingUserMutation>(mutate, {
          variables: { input: { userOid } }
        }, functionName);
      }
    })
  });
