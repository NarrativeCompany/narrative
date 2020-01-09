import * as React from 'react';
import { branch, compose, renderComponent, withProps } from 'recompose';
import { MemberProfileConnect, WithMemberProfileProps } from '../../../../shared/context/MemberProfileContext';
import { FormattedMessage } from 'react-intl';
import { MemberFollowsMessages } from '../../../../shared/i18n/MemberFollowsMessages';
import { FollowListHiddenWarning } from './FollowListHiddenWarning';
import { TabDetails, TabRoute } from '../../../../shared/containers/withTabsController';
import { WebRoute } from '../../../../shared/constants/routes';
import * as Routes from '../../../index';
import { withTabBarController, WithTabBarControllerProps } from '../../../../shared/containers/withTabBarController';
import styled from '../../../../shared/styled';

const NavBarContainer = styled.div`
  margin-top: 20px;
`;

type Props =
  WithMemberProfileProps &
  WithTabBarControllerProps;

const MemberFollowedItemsComponent: React.SFC<Props> = (props) => {
  const { tabBar, selectedTabRoute, detailsForProfile: { hideMyFollows } } = props;

  return (
    <React.Fragment>
      <NavBarContainer>{tabBar}</NavBarContainer>

      {/* if the follows are hidden, then we know we are viewing our own profile and need to be warned */}
      {hideMyFollows && <FollowListHiddenWarning/>}

      {selectedTabRoute}
    </React.Fragment>
  );
};

export default compose(
  MemberProfileConnect,
  // jw: if the viewer cannot see the list, then let's just short out with that error.
  branch((props: WithMemberProfileProps) => !props.isForCurrentUser && props.detailsForProfile.hideMyFollows,
    renderComponent((props: WithMemberProfileProps) => {
      const { detailsForProfile: { user: { displayName } } } = props;
      return <FormattedMessage {...MemberFollowsMessages.MemberHasFollowsHidden} values={{displayName}}/>;
    })
  ),
  // jw: at this point we know we should be showing the list. So let's create the tabs
  withProps((props: WithMemberProfileProps) => {
    const { detailsForProfile: { user: { username } } } = props;

    const tabs: TabDetails[] = [
      new TabDetails(
        // jw: default to the route for followed niches
        new TabRoute(WebRoute.UserProfileFollowedNiches),
        MemberFollowsMessages.Niches,
        Routes.MemberFollowedNiches
      ),
      new TabDetails(
        // jw: default to the route for followed publications
        new TabRoute(WebRoute.UserProfileFollowedPublications),
        MemberFollowsMessages.Publications,
        Routes.MemberFollowedPublications
      ),
      new TabDetails(
        // jw: this default is the route for followed niches
        new TabRoute(WebRoute.UserProfileFollowedUsers),
        MemberFollowsMessages.People,
        Routes.MemberFollowedUsers,
      ),
    ];

    return {
      tabs,
      tabRouteParams: { username }
    };
  }),
  withTabBarController
)(MemberFollowedItemsComponent) as React.ComponentClass<{}>;
