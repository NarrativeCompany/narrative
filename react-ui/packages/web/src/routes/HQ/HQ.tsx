import * as React from 'react';
import { Switch, Route, RouteComponentProps } from 'react-router-dom';
import { ViewWrapper } from '../../shared/components/ViewWrapper';
import { Row, Col } from 'antd';
import { HQNavMenu } from './HQNavMenu';
import * as Routes from '../index';
import { WebRoute } from '../../shared/constants/routes';
import { NotFound } from '../../shared/components/NotFound';

type Props = RouteComponentProps<{}>;

const HQ: React.SFC<Props> = () => {
  return (
    <ViewWrapper>
      <Row>
        <Col lg={4} xl={3}>
          <HQNavMenu/>
        </Col>

        <Col lg={20} xl={21}>
          <Switch>
            <Route path={WebRoute.Approvals} component={Routes.Approvals}/>
            <Route exact={true} path={WebRoute.AuctionInvoice} component={Routes.AuctionInvoice}/>
            <Route path={WebRoute.Auctions} component={Routes.Auctions}/>
            <Route exact={true} path={WebRoute.LeadershipTribunal} component={Routes.TribunalMembers}/>
            <Route path={WebRoute.Appeals} component={Routes.TribunalAppeals}/>
            <Route path={WebRoute.Moderators} component={Routes.ModeratorCenter}/>
            <Route path={WebRoute.NetworkStatsRewards} component={Routes.NetworkStatsRewards}/>
            <Route path={WebRoute.NetworkStats} component={Routes.NetworkStats}/>
            <Route component={NotFound}/>
          </Switch>
        </Col>
      </Row>
    </ViewWrapper>
  );
};

export default HQ;
