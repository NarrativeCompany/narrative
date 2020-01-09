import * as React from 'react';
import { compose, withProps } from 'recompose';
import {
  withFollowedItemPropsFromMemberProfileConnect
} from '../containers/withFollowedItemPropsFromMemberProfileConnect';
import { withFollowedPublications } from '@narrative/shared';
import { FollowList, FollowListWordingProps } from './FollowList';
import { MemberFollowsMessages } from '../../../../shared/i18n/MemberFollowsMessages';

export default compose(
  withFollowedItemPropsFromMemberProfileConnect,
  withFollowedPublications,
  withProps<FollowListWordingProps, {}>({
    seoTitle: MemberFollowsMessages.PublicationsSeoTitle,
    seoDescription: MemberFollowsMessages.PublicationsSeoDescription,
    noResultsMessage: MemberFollowsMessages.NoFollowedPublications,
    noResultsMessageForCurrentUser: MemberFollowsMessages.NoFollowedPublicationsForCurrentUser
  })
)(FollowList) as React.ComponentClass<{}>;
