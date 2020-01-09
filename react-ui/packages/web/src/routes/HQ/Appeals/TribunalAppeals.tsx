import * as React from 'react';
import { FormattedMessage } from 'react-intl';
import { Redirect, Route, RouteComponentProps } from 'react-router';
import { compose, withProps } from 'recompose';
import { TabLink, TabPane, Tabs } from '../../../shared/components/Tabs';
import { PageHeader } from '../../../shared/components/PageHeader';
import { SEO } from '../../../shared/components/SEO';
import { WebRoute } from '../../../shared/constants/routes';
import { TribunalAppealsMessages } from '../../../shared/i18n/TribunalAppealsMessages';
import { TribunalAppealsMyQueueList } from './TribunalAppealsMyQueueList';
import { TribunalAppealsUnderReviewList } from './TribunalAppealsUnderReviewList';
import { TribunalAppealsWithCompletedReviewList } from './TribunalAppealsWithCompletedReviewList';
import {
  withExtractedCurrentUser,
  WithExtractedCurrentUserProps
} from '../../../shared/containers/withExtractedCurrentUser';
import { SEOMessages } from '../../../shared/i18n/SEOMessages';
import { NotFound } from '../../../shared/components/NotFound';
import { isPermissionGranted } from '../../../shared/containers/withPermissionsModalController';

interface WithProps {
  defaultActiveKey: string;
  canParticipateInTribunalActions: boolean;
}

type Props =
  RouteComponentProps<{}> &
  WithExtractedCurrentUserProps &
  WithProps;

const TribunalAppealsComponent: React.SFC<Props> = (props) => {
  const { defaultActiveKey, canParticipateInTribunalActions } = props;

  return (
    <React.Fragment>
      <SEO
        title={SEOMessages.AppealsTitle}
        description={SEOMessages.AppealsDescription}
      />

      <PageHeader
        preTitle={<FormattedMessage {...TribunalAppealsMessages.PageHeaderPreTitle}/>}
        title={<FormattedMessage {...TribunalAppealsMessages.PageHeaderTitle}/>}
        description={<FormattedMessage {...TribunalAppealsMessages.HeadingText}/>}
        iconType="appeals"
      />

      {!defaultActiveKey &&
      <NotFound/>}

      {defaultActiveKey &&
      <React.Fragment>
        <Tabs
          defaultActiveKey={defaultActiveKey}
          activeKey={defaultActiveKey}
          style={{ padding: '0 5px' }}
        >
          {canParticipateInTribunalActions &&
          <TabPane
            key={WebRoute.AppealsMyQueue}
            tab={<TabLink route={WebRoute.AppealsMyQueue} title={TribunalAppealsMessages.MyQueueTabTitle}/>}
          >
            <Route path={WebRoute.AppealsMyQueue} component={TribunalAppealsMyQueueList}/>
          </TabPane>}

          <TabPane
            key={WebRoute.AppealsUnderReview}
            tab={<TabLink route={WebRoute.AppealsUnderReview} title={TribunalAppealsMessages.UnderReviewTabTitle}/>}
          >
            <Route path={WebRoute.AppealsUnderReview} component={TribunalAppealsUnderReviewList}/>
          </TabPane>

          <TabPane
            key={WebRoute.AppealsCompletedReview}
            tab={<TabLink route={WebRoute.AppealsCompletedReview} title={TribunalAppealsMessages.CompletedTabTitle}/>}
          >
            <Route path={WebRoute.AppealsCompletedReview} component={TribunalAppealsWithCompletedReviewList}/>
          </TabPane>
        </Tabs>

        <Route exact={true} path={WebRoute.Appeals} render={() => <Redirect to={defaultActiveKey}/>}/>
      </React.Fragment>}
    </React.Fragment>
  );
};

export default compose(
  withExtractedCurrentUser,
  withProps((props: RouteComponentProps<{}> & WithExtractedCurrentUserProps) => {
    const { location, currentUserGlobalPermissions } = props;

    const canParticipateInTribunalActions = isPermissionGranted(
      'participateInTribunalActions',
      currentUserGlobalPermissions
    );

    const noMatchDefaultRoute =
      canParticipateInTribunalActions ? WebRoute.AppealsMyQueue : WebRoute.AppealsUnderReview;
    let defaultActiveKey;

    if (location.pathname === WebRoute.Appeals || location.pathname === `${WebRoute.Appeals}/`) {
      defaultActiveKey = noMatchDefaultRoute;
    } else if (location.pathname.startsWith(WebRoute.AppealsMyQueue)) {
      defaultActiveKey = WebRoute.AppealsMyQueue;
    } else if (location.pathname.startsWith(WebRoute.AppealsUnderReview)) {
      defaultActiveKey = WebRoute.AppealsUnderReview;
    } else if (location.pathname.startsWith(WebRoute.AppealsCompletedReview)) {
      defaultActiveKey = WebRoute.AppealsCompletedReview;
    } else {
      defaultActiveKey = null;
    }

    return { canParticipateInTribunalActions, defaultActiveKey };
  }),
)(TribunalAppealsComponent) as React.ComponentClass<{}>;
