import * as React from 'react';
import { Redirect, Route, Switch } from 'react-router';
import { TribunalAppealsWithCompletedReviewListPage } from './TribunalAppealsWithCompletedReviewListPage';
import { WebRoute } from '../../../shared/constants/routes';

export const TribunalAppealsWithCompletedReviewList: React.SFC<{}> = () => {
  return (
    <Switch>
      <Route
        exact={true}
        path={WebRoute.AppealsCompletedReviewPage}
        component={TribunalAppealsWithCompletedReviewListPage}
      />
      <Route
        exact={true}
        path={WebRoute.AppealsCompletedReview}
        render={() => <Redirect to={`${WebRoute.AppealsCompletedReview}/1`}/>}
      />
    </Switch>
  );
};
