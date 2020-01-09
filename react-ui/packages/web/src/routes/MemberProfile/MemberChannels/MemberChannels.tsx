import * as React from 'react';
import { compose, withProps } from 'recompose';
import { MemberProfileConnect, WithMemberProfileProps } from '../../../shared/context/MemberProfileContext';
import { TabDetails, TabRoute } from '../../../shared/containers/withTabsController';
import { WebRoute } from '../../../shared/constants/routes';
import * as Routes from '../../index';
import { withPillMenuController, WithPillMenuControllerProps } from '../../../shared/containers/withPillMenuController';
import { MemberChannelsMessages } from '../../../shared/i18n/MemberChannelsMessages';
import { PillMenu } from '../../../shared/components/navigation/pills/PillMenu';

const MemberChannelsComponent: React.SFC<WithPillMenuControllerProps> = (props) => {
  const { pillMenuProps, selectedTabRoute } = props;

  return (
    <React.Fragment>
      <PillMenu {...pillMenuProps} />

      {selectedTabRoute}

    </React.Fragment>
  );
};

export const MemberChannels = compose(
  MemberProfileConnect,
  withProps((props: WithMemberProfileProps) => {
    const { detailsForProfile: { user: { username } } } = props;

    const tabs: TabDetails[] = [
      new TabDetails(
        // jw: default to the route for niche assocaitions
        new TabRoute(WebRoute.UserProfileNiches),
        MemberChannelsMessages.NichesTab,
        Routes.MemberNiches
      ),
      new TabDetails(
        new TabRoute(WebRoute.UserProfilePublications),
        MemberChannelsMessages.PublicationsTab,
        Routes.MemberPublications
      ),
    ];

    return {
      tabs,
      tabRouteParams: { username }
    };
  }),
  withPillMenuController
)(MemberChannelsComponent) as React.ComponentClass<{}>;

export default MemberChannels;
