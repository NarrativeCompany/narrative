import * as React from 'react';
import { generatePath, Redirect, Route, Switch } from 'react-router';
import { ActiveAuctionListPage } from './ActiveAuctionListPage';
import { WebRoute } from '../../../shared/constants/routes';

export const ActiveAuctionList: React.SFC<{}> = () => {
  return (
    <Switch>
      <Route exact={true} path={WebRoute.AuctionsActivePage} component={ActiveAuctionListPage}/>
      <Route
        exact={true}
        path={WebRoute.AuctionsActive}
        render={() => <Redirect to={generatePath(WebRoute.AuctionsActivePage, { page: 1 })}/>}
      />
    </Switch>
  );
};
