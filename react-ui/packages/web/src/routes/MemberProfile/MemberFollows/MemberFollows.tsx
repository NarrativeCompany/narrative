import * as React from 'react';
import { compose, withProps } from 'recompose';
import { MemberFollowsMessages } from '../../../shared/i18n/MemberFollowsMessages';
import { MemberProfileConnect, WithMemberProfileProps } from '../../../shared/context/MemberProfileContext';
import { TabDetails, TabRoute, TabSubRoute } from '../../../shared/containers/withTabsController';
import { WebRoute } from '../../../shared/constants/routes';
import * as Routes from '../../index';
import { withPillMenuController, WithPillMenuControllerProps } from '../../../shared/containers/withPillMenuController';
import { PillMenu } from '../../../shared/components/navigation/pills/PillMenu';

type Props =
  WithPillMenuControllerProps;

const MemberFollowsComponent: React.SFC<Props> = (props) => {
  const { pillMenuProps, selectedTabRoute } = props;

  return (
    <React.Fragment>
      <PillMenu {...pillMenuProps} />

      {selectedTabRoute}

    </React.Fragment>
  );
};

export default compose(
  MemberProfileConnect,
  withProps((props: WithMemberProfileProps) => {
    const { detailsForProfile: { user: { username } } } = props;

    const tabs: TabDetails[] = [
      new TabDetails(
        // jw: default to the route for followed niches
        new TabRoute(WebRoute.UserProfileFollowedNiches),
        MemberFollowsMessages.Following,
        Routes.MemberFollowedItems,
        // jw: need to make sure that the followed users route also directs to this component
        [
          new TabSubRoute(WebRoute.UserProfileFollowedPublications, true, undefined, true),
          new TabSubRoute(WebRoute.UserProfileFollowedUsers, true, undefined, true)
        ]
      ),
      new TabDetails(
        // jw: this default is the route for followed niches
        new TabRoute(WebRoute.UserProfileFollowers),
        MemberFollowsMessages.Followers,
        Routes.MemberFollowers,
      ),
    ];

    return {
      tabs,
      tabRouteParams: { username }
    };
  }),
  withPillMenuController
)(MemberFollowsComponent) as React.ComponentClass<{}>;
