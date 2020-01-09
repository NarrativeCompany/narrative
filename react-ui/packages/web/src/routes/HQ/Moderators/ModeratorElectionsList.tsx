import * as React from 'react';
import { Redirect, Route, Switch } from 'react-router';
import { WebRoute } from '../../../shared/constants/routes';
import { ModeratorElectionsListPage } from './ModeratorElectionsListPage';

export const ModeratorElectionsList: React.SFC<{}> = () => {
  return (
    <Switch>
      <Route exact={true} path={WebRoute.ModeratorElectionsPage} component={ModeratorElectionsListPage}/>
      <Route
        exact={true}
        path={WebRoute.ModeratorElections}
        render={() => <Redirect to={`${WebRoute.ModeratorElections}/1`}/>}
      />
    </Switch>
  );
};
