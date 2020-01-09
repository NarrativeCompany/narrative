import * as React from 'react';
import { compose } from 'recompose';
import { WithMemberProfileProps } from '../../../../shared/context/MemberProfileContext';
import {
  withUserFollowers,
  WithUserFollowersProps
} from '@narrative/shared';
import { FormattedMessage } from 'react-intl';
import { MemberFollowsMessages } from '../../../../shared/i18n/MemberFollowsMessages';
import { Paragraph } from '../../../../shared/components/Paragraph';
import { ContainedLoading } from '../../../../shared/components/Loading';
import { FollowListHiddenWarning } from './FollowListHiddenWarning';
import {
  withFollowedItemPropsFromMemberProfileConnect
} from '../containers/withFollowedItemPropsFromMemberProfileConnect';
import { FollowList } from './FollowList';

type Props =
  WithMemberProfileProps &
  WithUserFollowersProps;

const MemberFollowersComponent: React.SFC<Props> = (props) => {
  const { loadMoreItemsLoading, items, loadMoreItems } = props;

  // jw: the only time we want to show the loading spinner is if this is the first time we are loading. Otherwise,
  //     we can just display the results we already have until new ones come in.
  if (loadMoreItemsLoading && !items.length) {
    return <ContainedLoading />;
  }

  const { isForCurrentUser, detailsForProfile: { hideMyFollowers, user: { displayName } }, totalFollowers } = props;

  // jw: if the user is hiding their follower list, let's give that as the only message.
  if (!isForCurrentUser && hideMyFollowers) {
    return (
      <FormattedMessage
        {...MemberFollowsMessages.MemberHasFollowersHidden}
        values={{displayName, totalFollowers}}
      />
    );
  }

  // jw: guess we are going to render this thing.
  const description = isForCurrentUser
    ? <FormattedMessage {...MemberFollowsMessages.YouHaveFollowers} values={{totalFollowers}}/>
    : <FormattedMessage {...MemberFollowsMessages.MemberHasFollowers} values={{displayName, totalFollowers}}/>;

  return (
    <React.Fragment>
      {/* if the followers are hidden, then we know we are viewing our own profile and need to be warned */}
      {hideMyFollowers && <FollowListHiddenWarning/>}

      <Paragraph marginBottom="large">
        {description}
      </Paragraph>

      {items.length > 0 &&
        <FollowList
          items={items}
          loadMoreItemsLoading={loadMoreItemsLoading}
          loadMoreItems={loadMoreItems}
          seoTitle={MemberFollowsMessages.FollowersSeoTitle}
          seoDescription={MemberFollowsMessages.FollowersSeoDescription}
        />
      }
    </React.Fragment>
  );
};

export default compose(
  withFollowedItemPropsFromMemberProfileConnect,
  withUserFollowers
)(MemberFollowersComponent) as React.ComponentClass<{}>;
