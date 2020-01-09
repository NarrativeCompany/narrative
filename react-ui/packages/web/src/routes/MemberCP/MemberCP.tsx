import * as React from 'react';
import { branch, compose, renderComponent, withProps } from 'recompose';
import { generatePath, Redirect } from 'react-router-dom';
import * as Routes from '../index';
import { WebRoute } from '../../shared/constants/routes';
import { ViewWrapper } from '../../shared/components/ViewWrapper';
import { UserDetail, withCurrentUserDetail, WithCurrentUserDetailProps } from '@narrative/shared';
import { viewWrapperPlaceholder, withLoadingPlaceholder } from '../../shared/utils/withLoadingPlaceholder';
import {
  TabDetails,
  TabRoute,
  TabsControllerParentProps,
  TabSubRoute
} from '../../shared/containers/withTabsController';
import { MemberCpMessages } from '../../shared/i18n/MemberCpMessages';
import { Card } from '../../shared/components/Card';
import { PageHeader } from '../../shared/components/PageHeader';
import { FormattedMessage } from 'react-intl';
import { Link } from '../../shared/components/Link';
import { MemberProfileWrapperMessages } from '../../shared/i18n/MemberProfileWrapperMessages';
import { withTabBarController, WithTabBarControllerProps } from '../../shared/containers/withTabBarController';

interface WithProps {
  userDetail: UserDetail;
}

type Props = WithProps &
  WithTabBarControllerProps;

const MemberCpComponent: React.SFC<Props> = (props) => {
  const { userDetail: { user: { username } }, tabBar, selectedTabRoute } = props;

  return (
    <ViewWrapper>
      <Card>
        <PageHeader
          title={<FormattedMessage {...MemberCpMessages.MemberCpTitle} />}
          description={
            <React.Fragment>
              <Link to={generatePath(WebRoute.UserProfile, {username})}>
                <FormattedMessage {...MemberProfileWrapperMessages.ViewYourProfile}/>
              </Link>
            </React.Fragment>
          }
        />

        {tabBar}

        {selectedTabRoute}
      </Card>
    </ViewWrapper>
  );
};

export default compose(
  withCurrentUserDetail,
  withProps((props: WithCurrentUserDetailProps) => {
    const { currentUserDetailData } = props;

    const { getCurrentUserDetail, loading } = currentUserDetailData;

    return { userDetail: getCurrentUserDetail, loading };
  }),
  withLoadingPlaceholder(viewWrapperPlaceholder()),
  // jw: if we did not get any details then redirect to signin
  branch<WithProps>((props) => !props.userDetail,
    renderComponent(() => <Redirect to={WebRoute.Signin} />)
  ),
  withProps<TabsControllerParentProps, {}>((): TabsControllerParentProps => {
    const tabs: TabDetails[] = [
      new TabDetails(
        // jw: default to the route for edit profile
        new TabRoute(WebRoute.MemberCP),
        MemberCpMessages.MemberCpProfile,
        Routes.MemberEditProfile,
        [new TabSubRoute(WebRoute.MemberEditProfile)]
      ),
      new TabDetails(
        new TabRoute(WebRoute.MemberCertification),
        MemberCpMessages.MemberCpCertification,
        Routes.MemberCertification,
      ),
      new TabDetails(
        new TabRoute(WebRoute.MemberAccountSettings),
        MemberCpMessages.MemberCpAccount,
        Routes.MemberAccountSettings,
      ),
      new TabDetails(
        new TabRoute(WebRoute.MemberSecuritySettings),
        MemberCpMessages.MemberCpSecurity,
        Routes.MemberSecuritySettings,
      ),
      new TabDetails(
        new TabRoute(WebRoute.MemberPersonalSettings),
        MemberCpMessages.MemberCpPreferences,
        Routes.MemberPersonalSettings,
      ),
      new TabDetails(
        new TabRoute(WebRoute.MemberNotificationSettings),
        MemberCpMessages.MemberCpNotifications,
        Routes.MemberNotificationSettings,
      ),
      new TabDetails(
        new TabRoute(WebRoute.MemberNeoWallet),
        MemberCpMessages.MemberCpNeoWallet,
        Routes.MemberNeoWallet,
      ),
      new TabDetails(
        new TabRoute(WebRoute.MemberManagePosts, false),
        MemberCpMessages.MemberCpPosts,
        Routes.MemberManagePosts,
      ),
    ];

    return { tabs };
  }),
  withTabBarController
)(MemberCpComponent) as React.ComponentClass<{}>;
