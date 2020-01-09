import * as React from 'react';
import { Redirect, Route, Switch } from 'react-router';
import { PendingPaymentListPage } from './PendingPaymentListPage';
import { WebRoute } from '../../../shared/constants/routes';

export const PendingPaymentList: React.SFC<{}> = () => {
  return (
    <Switch>
      <Route exact={true} path={WebRoute.AuctionsPendingPaymentPage} component={PendingPaymentListPage}/>
      <Route
        exact={true}
        path={WebRoute.AuctionsPendingPayment}
        render={() => <Redirect to={`${WebRoute.AuctionsPendingPayment}/1`}/>}
      />
    </Switch>
  );
};
