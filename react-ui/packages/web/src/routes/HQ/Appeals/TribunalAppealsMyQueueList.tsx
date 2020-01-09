import * as React from 'react';
import { Redirect, Route, Switch } from 'react-router';
import { TribunalAppealsMyQueueListPage } from './TribunalAppealsMyQueueListPage';
import { WebRoute } from '../../../shared/constants/routes';

export const TribunalAppealsMyQueueList: React.SFC<{}> = () => {

  return (
    <Switch>
      <Route exact={true} path={WebRoute.AppealsMyQueuePage} component={TribunalAppealsMyQueueListPage}/>
      <Route
        exact={true}
        path={WebRoute.AppealsMyQueue}
        render={() => <Redirect to={`${WebRoute.AppealsMyQueue}/1`}/>}
      />
    </Switch>
  );
};
