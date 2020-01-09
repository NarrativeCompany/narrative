import * as React from 'react';
import { compose } from 'recompose';
import { FormattedMessage } from 'react-intl';
import { Redirect, Route } from 'react-router-dom';
import { TabLink, TabPane, Tabs } from '../../../shared/components/Tabs';
import { SEO } from '../../../shared/components/SEO';
import { PageHeader } from '../../../shared/components/PageHeader';
import { ModeratorElectionsList } from './ModeratorElectionsList';
import { SEOMessages } from '../../../shared/i18n/SEOMessages';
import { ModCenterMessages } from '../../../shared/i18n/ModCenterMessages';
import { WebRoute } from '../../../shared/constants/routes';

const ModeratorCenterComponent: React.SFC<{}> = () => {
  return (
    <React.Fragment>
      <SEO
        title={SEOMessages.ModCenterJobsTitle}
        description={SEOMessages.ModCenterJobsDescription}
      />

      <PageHeader
        iconType="moderators"
        preTitle={<FormattedMessage {...ModCenterMessages.PageHeaderPreTitle}/>}
        title={<FormattedMessage {...ModCenterMessages.PageHeaderTitle}/>}
        description={<FormattedMessage {...ModCenterMessages.PageHeaderDescription}/>}
      />

      <Tabs
        defaultActiveKey={WebRoute.Moderators}
        activeKey={WebRoute.Moderators}
        style={{padding: '0 5px'}}
      >
        <TabPane
          tab={<TabLink title={ModCenterMessages.ModeratorElectionsTab} route={WebRoute.Moderators}/>}
          key={WebRoute.Moderators}
        >
          <Route path={WebRoute.ModeratorElections} component={ModeratorElectionsList}/>
        </TabPane>

        <TabPane disabled={true} tab={<FormattedMessage {...ModCenterMessages.ModeratorPoolTab}/>} key="moderatorPool"/>
      </Tabs>

      <Route exact={true} path={WebRoute.Moderators} render={() => <Redirect to={WebRoute.ModeratorElections}/>}/>
    </React.Fragment>
  );
};

export default compose(
)(ModeratorCenterComponent) as React.ComponentClass<{}>;
