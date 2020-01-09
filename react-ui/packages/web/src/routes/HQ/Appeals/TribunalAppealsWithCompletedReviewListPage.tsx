import * as React from 'react';
import { compose } from 'recompose';
import { WebRoute } from '../../../shared/constants/routes';
import {
  withAllNichesWithCompletedTribunalReview,
  WithAllNichesWithCompletedTribunalReviewProps
} from '@narrative/shared';
import { TribunalAppealsBaseListComponent } from './TribunalAppealsBaseList';
import { withPaginationController } from '../../../shared/containers/withPaginationController';

export const TribunalAppealsWithCompletedReviewListPage = compose(
  withPaginationController<WithAllNichesWithCompletedTribunalReviewProps>(
    withAllNichesWithCompletedTribunalReview,
    WebRoute.AppealsCompletedReview
  )
)(TribunalAppealsBaseListComponent) as React.ComponentClass<{}>;
