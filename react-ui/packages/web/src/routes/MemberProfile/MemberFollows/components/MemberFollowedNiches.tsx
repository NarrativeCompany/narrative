import * as React from 'react';
import { compose, withProps } from 'recompose';
import {
  withFollowedItemPropsFromMemberProfileConnect
} from '../containers/withFollowedItemPropsFromMemberProfileConnect';
import { withFollowedNiches } from '@narrative/shared';
import { FollowList, FollowListWordingProps } from './FollowList';
import { MemberFollowsMessages } from '../../../../shared/i18n/MemberFollowsMessages';

export default compose(
  withFollowedItemPropsFromMemberProfileConnect,
  withFollowedNiches,
  withProps<FollowListWordingProps, {}>({
    seoTitle: MemberFollowsMessages.NichesSeoTitle,
    seoDescription: MemberFollowsMessages.NichesSeoDescription,
    noResultsMessage: MemberFollowsMessages.NoFollowedNiches,
    noResultsMessageForCurrentUser: MemberFollowsMessages.NoFollowedNichesForCurrentUser
  })
)(FollowList) as React.ComponentClass<{}>;
