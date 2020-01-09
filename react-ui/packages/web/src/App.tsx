import * as React from 'react';
import { Redirect, Route, RouteComponentProps, Switch, withRouter } from 'react-router';
import { AuthRoute } from './shared/components/AuthRoute';
import { WebRoute } from './shared/constants/routes';
import { Layout } from './layout/Layout';
import * as Routes from './routes';
import * as ReactGA from 'react-ga';
import { compose, lifecycle } from 'recompose';
import { GlobalErrorController } from './layout/Content/GlobalErrorController';
import { GuestRoute } from './shared/components/GuestRoute';
import { NotFound } from './shared/components/NotFound';
import { Search } from './routes/Search/Search';

type Props =
  RouteComponentProps<{}>;

const AppComponent: React.SFC<Props> = () => {

  const appRoutes = (
    <Layout>
      <Switch>
        {/* legacy URLs */}
        <Redirect from="/join/:emailAddress?" to={WebRoute.Register} />
        <Redirect from="/ballot-box" to={WebRoute.Approvals}/>
        <Redirect from="/auctions" to={WebRoute.Auctions}/>
        <Redirect from="/tribunal" to={WebRoute.LeadershipTribunal}/>
        <Redirect from="/tribunal-appeals" to={WebRoute.AppealsUnderReview}/>
        <Redirect from="/completed-tribunal-appeals" to={WebRoute.AppealsCompletedReview}/>
        <Redirect from="/tribunal-appeals-queue" to={WebRoute.AppealsMyQueue}/>

        <Redirect from="/member-cp/notifications" to={WebRoute.MemberNotificationSettings}/>
        <Redirect from="/member-cp/update-profile" to={WebRoute.MemberCP}/>

        <Redirect from="/member/:username" to={WebRoute.UserProfile}/>
        <Redirect from="/niche/:id" to={WebRoute.NicheDetails}/>
        <Redirect from="/ballot/:referendumOid" to={WebRoute.ApprovalDetails}/>
        <Redirect from="/auction/:auctionOid" to={WebRoute.AuctionDetails}/>
        <Redirect from="/invoice/:invoiceOid" to={WebRoute.AuctionInvoice}/>

        <Redirect from="/confirmEmail/u/:userOid/cid/:confirmationToken" to={WebRoute.ConfirmEmail}/>
        <Redirect from="/resetPassword/u/:userOid/t/:timestamp/k/:key" to={WebRoute.ResetPassword}/>

        {/* standard routes */}
        <Route exact={true} path={WebRoute.Home} component={Routes.Home}/>
        <Route exact={true} path={WebRoute.HomeParameterized} component={Routes.Home}/>
        <Route exact={true} path={WebRoute.Discover} component={Routes.Discover}/>
        <Route exact={true} path={WebRoute.DiscoverParameterized} component={Routes.Discover}/>
        <Route exact={true} path={WebRoute.HQ} component={Routes.HQLanding}/>
        <Route path={WebRoute.Approvals} component={Routes.HQ}/>
        <Route path={WebRoute.Auctions} component={Routes.HQ}/>
        <Route path={WebRoute.LeadershipTribunal} component={Routes.HQ}/>
        <Route path={WebRoute.Appeals} component={Routes.HQ}/>
        <Route path={WebRoute.AuctionInvoice} component={Routes.HQ}/>
        <Route path={WebRoute.Moderators} component={Routes.HQ}/>
        <Route path={WebRoute.NetworkStatsRewards} component={Routes.HQ}/>
        <Route path={WebRoute.NetworkStats} component={Routes.HQ}/>
        <Route path={WebRoute.UserProfile} component={Routes.MemberProfile}/>
        <Route path={WebRoute.Search} component={Search}/>
        <Route exact={true} path={WebRoute.AppealDetails} component={Routes.TribunalAppealDetails}/>
        <Route path={WebRoute.NicheDetails} component={Routes.NicheDetails}/>
        <Route exact={true} path={WebRoute.ApprovalDetails} component={Routes.ApprovalDetails}/>
        <Route exact={true} path={WebRoute.AuctionDetails} component={Routes.AuctionDetails}/>
        <Route exact={true} path={WebRoute.ModeratorElectionDetails} component={Routes.ModeratorElectionDetails}/>
        <Route exact={true} path={WebRoute.ConfirmEmail} component={Routes.ConfirmEmail}/>
        <Route exact={true} path={WebRoute.ConfirmEmailChange} component={Routes.ConfirmEmailChange}/>
        <Route exact={true} path={WebRoute.CancelEmailChange} component={Routes.CancelEmailChange}/>
        <Route exact={true} path={WebRoute.NicheExplainer} component={Routes.NicheExplainer}/>
        <Route exact={true} path={WebRoute.PublicationExplainer} component={Routes.PublicationExplainer}/>
        <Route exact={true} path={WebRoute.NRVEExplainer} component={Routes.NRVEExplainer}/>
        <Route exact={true} path={WebRoute.RewardsExplainer} component={Routes.RewardsExplainer}/>
        <Route exact={true} path={WebRoute.CertificationExplainer} component={Routes.CertificationExplainer}/>
        <Route exact={true} path={WebRoute.NrveWalletsExplainer} component={Routes.NeoWalletsExplainer}/>
        <Route exact={true} path={WebRoute.PostDetails} component={Routes.PostDetail}/>

        {/*
          jw: we want to ensure that all of the Publication CP routes are secured and require authentication. To do that
              we ned to match the base of all of those and direct them through the standard PublicationLayout component.
              note: we also want to protect the invitation route similarly
        */}
        <AuthRoute path={WebRoute.PublicationCP} component={Routes.PublicationLayout}/>
        <AuthRoute exact={true} path={WebRoute.PublicationInvitation} component={Routes.PublicationLayout}/>
        <Route path={WebRoute.PublicationDetails} component={Routes.PublicationLayout}/>

        {/* Protected Routes */}
        <AuthRoute exact={true} path={WebRoute.SuggestNiche} component={Routes.SuggestNiche}/>
        <AuthRoute exact={true} path={WebRoute.CreatePublication} component={Routes.CreatePublication}/>
        <AuthRoute path={WebRoute.MemberCertificationForm} component={Routes.MemberCertificationForm}/>
        <AuthRoute path={WebRoute.MemberCP} component={Routes.MemberCP}/>

        {/* No Match Route */}
        <Route component={NotFound}/>

      </Switch>
    </Layout>
  );

  return (
    <React.Fragment>
      <Switch>
        <GuestRoute exact={true} path={WebRoute.Signin} component={Routes.Login}/>
        <GuestRoute exact={true} path={WebRoute.ResetPassword} component={Routes.ResetPassword}/>
        <GuestRoute exact={true} path={WebRoute.Register} component={Routes.Register}/>
        <AuthRoute exact={true} path={WebRoute.Post} component={Routes.Post}/>
        <Route exact={true} path={WebRoute.SigninVerify} component={Routes.LoginVerify}/>
        <Route exact={true} path={WebRoute.Unsubscribe} component={Routes.SuspendEmail}/>
        <Route render={() => appRoutes}/>
      </Switch>

      {/* Handle global errors */}
      <GlobalErrorController/>

    </React.Fragment>
  );
};

export const App = compose(
  withRouter,
  lifecycle<Props, {}>({
    componentDidMount() {
      const { history } = this.props;
      // Initialize Google Analytics
      ReactGA.initialize('UA-108535722-1');

      // Add a history listener to send GA messages on location change
      history.listen(location => ReactGA.pageview(location.pathname));
    }
  })
)(AppComponent)as React.ComponentClass<{}>;
