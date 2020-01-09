import * as React from 'react';
import { Redirect, Route, Switch } from 'react-router';
import { TribunalAppealsUnderReviewListPage } from './TribunalAppealsUnderReviewListPage';
import { WebRoute } from '../../../shared/constants/routes';

export const TribunalAppealsUnderReviewList: React.SFC<{}> = () => {

  return (
    <Switch>
      <Route exact={true} path={WebRoute.AppealsUnderReviewPage} component={TribunalAppealsUnderReviewListPage}/>
      <Route
        exact={true}
        path={WebRoute.AppealsUnderReview}
        render={() => <Redirect to={`${WebRoute.AppealsUnderReview}/1`}/>}
      />
    </Switch>
  );
};
