import * as React from 'react';
import { branch, compose, withHandlers, withProps } from 'recompose';
import { FollowButton, FollowButtonParentHandlers } from '../FollowButton';
import {
  User,
  withCurrentUserFollowingUser,
  WithCurrentUserFollowingUserProps,
  CurrentUserFollowedItem,
  withStartFollowingUser,
  WithStartFollowingUserProps,
  withStopFollowingUser,
  WithStopFollowingUserProps
} from '@narrative/shared';
import { withExtractedCurrentUser, WithExtractedCurrentUserProps } from '../../containers/withExtractedCurrentUser';

interface ParentProps {
  user: User;
  // jw: in some cases we will need to get this from the outside.
  followedUser?: CurrentUserFollowedItem;
}

interface Props extends FollowButtonParentHandlers, ParentProps {
  loading?: boolean;
}

const FollowMemberButtonComponent: React.SFC<Props> = (props) => {
  const { loading, followedUser, handleStartFollowing, handleStopFollowing } = props;

  return (
    <FollowButton
      followedItem={followedUser}
      handleStartFollowing={handleStartFollowing}
      handleStopFollowing={handleStopFollowing}
      loading={loading}
    />
  );
};

type HandlersProps =
  WithStartFollowingUserProps &
  WithStopFollowingUserProps & {
    userOid: string;
  };

export const FollowMemberButton = compose(
  withProps((props: ParentProps) => {
    return { userOid: props.user.oid };
  }),
  withExtractedCurrentUser,
  // jw: only include withCurrentUserFollowing if we were not explicitly given a followedUser, and the viewer is
  //     a logged in user.
  branch((props: ParentProps & WithExtractedCurrentUserProps) => !props.followedUser && !!props.currentUser,
    compose(
      withCurrentUserFollowingUser,
      // jw: the followedUser will already be added with the right name when present, so let's just convert the
      withProps((props: WithCurrentUserFollowingUserProps) => {
        const loading = props.followedUserLoading;

        return { loading };
      })
    )
  ),
  withStartFollowingUser,
  withStopFollowingUser,
  withHandlers<HandlersProps, FollowButtonParentHandlers>({
    handleStartFollowing: (props) => async () => {
      const { userOid, startFollowingUser } = props;

      await startFollowingUser(userOid);
    },
    handleStopFollowing: (props) => async () => {
      const { userOid, stopFollowingUser } = props;

      await stopFollowingUser(userOid);
    }
  })
)(FollowMemberButtonComponent) as React.ComponentClass<ParentProps>;

/**
 * jw: We should only be including the follow user button if the current user is not that user. To centralize that logic
 *     I created this HOC and am exposing it from here since this is what is truly effected by its result.
 */

export interface WithIncludeFollowMemberButtonProps {
  includeFollowButton: boolean;
}

export const withIncludeFollowMemberButton = compose(
  withExtractedCurrentUser,
  withProps((props: ParentProps & WithExtractedCurrentUserProps) => {
    const { user, currentUser, currentUserLoading } = props;

    // jw: we should include the button for guests, or other users viewing this card.
    const includeFollowButton = !currentUserLoading && (!currentUser || currentUser.oid !== user.oid);

    return { includeFollowButton };
  })
);
