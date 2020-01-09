import * as React from 'react';
import { compose, withProps } from 'recompose';
import { Route, RouteComponentProps, Redirect } from 'react-router-dom';
import { TabLink, TabPane, Tabs } from '../../../shared/components/Tabs';
import { PageHeader } from '../../../shared/components/PageHeader';
import { SEO } from '../../../shared/components/SEO';
import { BidListMessages } from '../../../shared/i18n/BidListMessages';
import { SuggestNicheButton } from '../components/SuggestNicheButton';
import { PendingPaymentList } from './PendingPaymentList';
import { ActiveAuctionList } from './ActiveAuctionList';
import { FormattedMessage, } from 'react-intl';
import { WebRoute } from '../../../shared/constants/routes';
import { SEOMessages } from '../../../shared/i18n/SEOMessages';
import { NotFound } from '../../../shared/components/NotFound';

interface WithProps {
  defaultActiveKey: string | null;
}

type Props =
  RouteComponentProps<{}> &
  WithProps;

const AuctionsComponent: React.SFC<Props> = (props) => {
  const { defaultActiveKey } = props;

  const PageHeaderDescription = (
    <React.Fragment>
      <FormattedMessage {...BidListMessages.PageHeaderDescription}/>&nbsp;
    </React.Fragment>
  );

  return (
    <React.Fragment>
      <SEO
        title={SEOMessages.AuctionsTitle}
        description={SEOMessages.AuctionsDescription}
      />

      <PageHeader
        preTitle={<FormattedMessage {...BidListMessages.PageHeaderPreTitle}/>}
        title={<FormattedMessage {...BidListMessages.PageHeaderTitle}/>}
        description={PageHeaderDescription}
        extra={<SuggestNicheButton/>}
        iconType="bid"
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
          <TabPane
            tab={<TabLink title={BidListMessages.ActiveTab} route={WebRoute.AuctionsActive}/>}
            key={WebRoute.AuctionsActive}
          >
            <Route path={WebRoute.AuctionsActive} component={ActiveAuctionList}/>
          </TabPane>

          <TabPane
            tab={<TabLink title={BidListMessages.PendingTab} route={WebRoute.AuctionsPendingPayment}/>}
            key={WebRoute.AuctionsPendingPayment}
          >
            <Route path={WebRoute.AuctionsPendingPayment} component={PendingPaymentList}/>
          </TabPane>
        </Tabs>

        <Route exact={true} path={WebRoute.Auctions} render={() => <Redirect to={defaultActiveKey}/>}/>
      </React.Fragment>}
    </React.Fragment>
  );
};

export default compose(
  withProps((props: RouteComponentProps<{}>) => {
    const { location } = props;

    let defaultActiveKey;

    if (location.pathname === WebRoute.Auctions || location.pathname === `${WebRoute.Auctions}/`) {
      defaultActiveKey = WebRoute.AuctionsActive;
    } else if (location.pathname.startsWith(WebRoute.AuctionsActive)) {
      defaultActiveKey = WebRoute.AuctionsActive;
    } else if (location.pathname.startsWith(WebRoute.AuctionsPendingPayment)) {
      defaultActiveKey = WebRoute.AuctionsPendingPayment;
    } else {
      defaultActiveKey = null;
    }

    return { defaultActiveKey };
  })
)(AuctionsComponent) as React.ComponentClass<{}>;
