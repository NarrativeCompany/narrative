import { graphql } from 'react-apollo';
import { startFollowingUserMutation } from '../graphql/user/startFollowingUserMutation';
import {

  StartFollowingUserMutation,
  StartFollowingUserMutation_startFollowingUser,
  StartFollowingUserMutationVariables,
} from '../../types';
import { mutationResolver } from '../../utils';

const functionName = 'startFollowingUser';

export interface WithStartFollowingUserProps {
  [functionName]: (userOid: string) => Promise<StartFollowingUserMutation_startFollowingUser>;
}

export const withStartFollowingUser =
  graphql<
    {},
    StartFollowingUserMutation,
    StartFollowingUserMutationVariables,
    WithStartFollowingUserProps
  >(startFollowingUserMutation, {
    props: ({mutate}) => ({
      [functionName]: async (userOid: string) => {
        return await mutationResolver<StartFollowingUserMutation>(mutate, {
          variables: { input: { userOid } }
        }, functionName);
      }
    })
  });
