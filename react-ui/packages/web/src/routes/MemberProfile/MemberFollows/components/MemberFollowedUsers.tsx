import * as React from 'react';
import { compose, withProps } from 'recompose';
import {
  withFollowedItemPropsFromMemberProfileConnect
} from '../containers/withFollowedItemPropsFromMemberProfileConnect';
import { withFollowedUsers } from '@narrative/shared';
import { FollowList, FollowListWordingProps } from './FollowList';
import { MemberFollowsMessages } from '../../../../shared/i18n/MemberFollowsMessages';

export default compose(
  withFollowedItemPropsFromMemberProfileConnect,
  withFollowedUsers,
  withProps<FollowListWordingProps, {}>({
    seoTitle: MemberFollowsMessages.PeopleSeoTitle,
    seoDescription: MemberFollowsMessages.PeopleSeoDescription,
    noResultsMessage: MemberFollowsMessages.NoFollowedPeople,
    noResultsMessageForCurrentUser: MemberFollowsMessages.NoFollowedPeopleForCurrentUser
  })
)(FollowList) as React.ComponentClass<{}>;
