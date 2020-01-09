import * as React from 'react';
import { generatePath, Redirect, RouteComponentProps } from 'react-router-dom';
import { MemberProfileWrapperMessages } from '../../shared/i18n/MemberProfileWrapperMessages';
import { InjectedIntlProps, injectIntl } from 'react-intl';
import { ViewWrapper } from '../../shared/components/ViewWrapper';
import { NotFound } from '../../shared/components/NotFound';
import { MemberProfileContext } from '../../shared/context/MemberProfileContext';
import { UserDetail, withUserDetail, WithUserDetailProps } from '@narrative/shared';
import { branch, compose, renderComponent, withProps } from 'recompose';
import * as Routes from '../index';
import { WebRoute } from '../../shared/constants/routes';
import { MemberProfilePageWrapper } from '../../shared/components/user/MemberProfilePageWrapper';
import {
  LoadingProps,
  viewWrapperPlaceholder,
  withLoadingPlaceholder
} from '../../shared/utils/withLoadingPlaceholder';
import {
  withExtractedCurrentUser,
  WithExtractedCurrentUserProps
} from '../../shared/containers/withExtractedCurrentUser';
import {
  CardTabsControllerStyleProps,
  withCardTabsController,
  WithCardTabsControllerProps
} from '../../shared/containers/withCardTabsController';
import {
  TabDetails,
  TabRoute,
  TabsControllerParentProps,
  TabSubRoute
} from '../../shared/containers/withTabsController';
import styled from '../../shared/styled';

export const MemberProfileHeaderText = styled.div`
  margin-bottom: 25px;
`;

interface UsernameProp {
  username: string;
}

interface Props extends WithCardTabsControllerProps, InjectedIntlProps {
  userDetails: UserDetail;
  isCurrentUser: boolean;
}

const MemberProfileByUsernameComponent: React.SFC<Props> = (props) => {
  const {
    userDetails,
    isCurrentUser,
    cardTabProps,
    narrowViewportMenu,
    selectedTabRoute
  } = props;

  if (!userDetails || !userDetails.user) {
    return <NotFound />;
  }

  return (
    <ViewWrapper>
      <MemberProfileContext.Provider value={{detailsForProfile: userDetails, isForCurrentUser: isCurrentUser}}>
        <MemberProfilePageWrapper
          cardProps={cardTabProps}
          userDetail={userDetails}
          isCurrentUser={isCurrentUser}
        >
          {narrowViewportMenu}
          {selectedTabRoute}
        </MemberProfilePageWrapper>
      </MemberProfileContext.Provider>
    </ViewWrapper>
  );
};

export default compose(
  // jw: first, ensure that we were given a username in the URL
  branch((props: RouteComponentProps<UsernameProp>) => !props.match.params.username,
    renderComponent(() => <NotFound/>)
  ),

  // jw: with that done, let's fetch the details that correspond to that username
  withProps((props: RouteComponentProps<UsernameProp>) => {
    const { match: { params } } = props;

    const username = params.username;

    return { username, userId: `id_${username}` };
  }),
  withUserDetail,
  // jw: next, let's load the user details and ensure that we get something back
  withProps<LoadingProps, WithUserDetailProps>((props) => {
    const { userDetailData: { loading } } = props;

    return { loading };
  }),
  withLoadingPlaceholder(viewWrapperPlaceholder()),
  branch(({userDetailData : {getUserDetail}}: WithUserDetailProps) => !getUserDetail || !getUserDetail.user,
    renderComponent(() => <NotFound/>)
  ),

  // jw: now that we are done loading the user details, let's parse it and prepare all our tabs.
  withExtractedCurrentUser,
  withProps((props: WithExtractedCurrentUserProps & WithUserDetailProps) => {
    const { userDetailData: { getUserDetail }, currentUser } = props;

    const isCurrentUser =
      currentUser &&
      getUserDetail &&
      getUserDetail.user &&
      getUserDetail.user.oid === currentUser.oid;

    return {
      isCurrentUser,
      userDetails: getUserDetail,
    };
  }),

  // jw: before we move any further, let's check to make sure the username matches case between the URL and member
  //     details. If not, then we need to redirect to fix that.
  branch((props: UsernameProp & Props) => props.username !== props.userDetails.user.username,
    renderComponent((props: UsernameProp & RouteComponentProps<{}> & Props) => {
      const { username, userDetails: { user }, history: { location } } = props;

      const currentPrefix = generatePath(WebRoute.UserProfile, { username });
      const newPrefix = generatePath(WebRoute.UserProfile, { username: user.username });

      if (!location.pathname.startsWith(currentPrefix)) {
        // todo:error-handling: This should never happen since we literally parsed the username out of the URL using
        //      the WebRoute.UserProfile route... Are we matching against something else now?
        return <NotFound/>;
      }

      // jw: let's replace the invalid cased version with the proper case and call it a day.
      return <Redirect to={location.pathname.replace(currentPrefix, newPrefix) + location.search}/>;
    })
  ),

  withProps<TabsControllerParentProps, UsernameProp>((props): TabsControllerParentProps => {
    const { username } = props;

    const tabs: TabDetails[] = [
      new TabDetails(
        new TabRoute(WebRoute.UserProfile),
        MemberProfileWrapperMessages.Journal,
        Routes.PersonalJournal,
        [new TabRoute(WebRoute.UserProfileJournal)]
      ),
      new TabDetails(
        new TabRoute(WebRoute.UserProfileActivity),
        MemberProfileWrapperMessages.Activity,
        Routes.MemberActivity
      ),
      new TabDetails(
        new TabRoute(WebRoute.UserProfileFollowedNiches),
        MemberProfileWrapperMessages.Follows,
        Routes.MemberFollows,
          [
            new TabSubRoute(WebRoute.UserProfileFollowedPublications, true, undefined, true),
            new TabSubRoute(WebRoute.UserProfileFollowedUsers, true, undefined, true),
            new TabSubRoute(WebRoute.UserProfileFollowers, true, undefined, true)
          ]
      ),
      new TabDetails(
        new TabRoute(WebRoute.UserProfileRewards),
        MemberProfileWrapperMessages.Rewards,
        Routes.MemberRewards,
        [new TabSubRoute(WebRoute.UserProfileRewardsTransactions)]
      ),
      new TabDetails(
        new TabRoute(WebRoute.UserProfileNiches),
        MemberProfileWrapperMessages.Channels,
        Routes.MemberChannels,
        [new TabSubRoute(WebRoute.UserProfilePublications, true, undefined, true)]
      ),
      new TabDetails(
        new TabRoute(WebRoute.UserProfileReputation),
        MemberProfileWrapperMessages.Reputation,
        Routes.MemberReputation
      ),
      new TabDetails(
        new TabRoute(WebRoute.UserProfileReferralProgram),
        MemberProfileWrapperMessages.ReferralProgram,
        Routes.MemberReferralDetails
      )
    ];

    return {
      tabs,
      tabRouteParams: { username }
    };
  }),
  withProps<CardTabsControllerStyleProps, {}>(() => {
    return {
      useNarrowTabs: true
    };
  }),
  withCardTabsController,
  injectIntl,
)(MemberProfileByUsernameComponent) as React.ComponentClass<{}>;
