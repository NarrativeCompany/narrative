import { compose, withProps } from 'recompose';
import { MemberProfileConnect, WithMemberProfileProps } from '../../../../shared/context/MemberProfileContext';
import { FollowListParentProps } from '@narrative/shared';

export const withFollowedItemPropsFromMemberProfileConnect = compose(
  MemberProfileConnect,
  // jw: first thing's first, let's setup the properties for the withUserFollowers
  withProps<FollowListParentProps, WithMemberProfileProps>((props) => {
    const userOid = props.detailsForProfile.user.oid;

    return { userOid };
  }),
);
