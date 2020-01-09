import { ChildDataProps, graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { currentUserFollowingUserQuery } from '../graphql/user/currentUserFollowingUserQuery';
import {
  CurrentUserFollowedItem,
  CurrentUserFollowingUserQuery,
  CurrentUserFollowingUserQueryVariables
} from '../../types';

const queryName = 'currentUserFollowingUserData';

interface ParentProps {
  userOid: string;
}

export interface WithCurrentUserFollowingUserProps {
  followedUserLoading: boolean;
  followedUser?: CurrentUserFollowedItem;
}

type WithProps = NamedProps<
  {[queryName]: GraphqlQueryControls & CurrentUserFollowingUserQuery},
  ChildDataProps<ParentProps, CurrentUserFollowingUserQuery>
>;

export const withCurrentUserFollowingUser =
  graphql<
    ParentProps,
    CurrentUserFollowingUserQuery,
    CurrentUserFollowingUserQueryVariables,
    WithCurrentUserFollowingUserProps
  >(currentUserFollowingUserQuery, {
    options: ({userOid}: ParentProps) => ({
      variables: { userOid }
    }),
    name: queryName,
    props: ({ currentUserFollowingUserData }: WithProps) => {
      const followedUserLoading =
        currentUserFollowingUserData.loading;

      const followedUser =
        currentUserFollowingUserData &&
        currentUserFollowingUserData.getCurrentUserFollowingUser;

      return { followedUserLoading, followedUser };
    }
  });
