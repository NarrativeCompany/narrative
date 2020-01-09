import * as React from 'react';
import { Redirect, Route, Switch } from 'react-router';
import { ApprovalListPage } from './ApprovalListPage';
import { WebRoute } from '../../../shared/constants/routes';

export const ApprovalListComponent: React.SFC<{}> = () => {
  return (
    <Switch>
      <Route exact={true} path={WebRoute.ApprovalsPage} component={ApprovalListPage}/>
      <Route
        exact={true}
        path={WebRoute.Approvals}
        render={() => <Redirect to={`${WebRoute.Approvals}/1`}/>}
      />
    </Switch>
  );
};

export default ApprovalListComponent;
