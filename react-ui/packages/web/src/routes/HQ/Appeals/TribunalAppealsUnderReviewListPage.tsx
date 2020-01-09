import * as React from 'react';
import { compose } from 'recompose';
import { WebRoute } from '../../../shared/constants/routes';
import {
  withAllNichesUnderTribunalReview,
  WithAllNichesUnderTribunalReviewProps
} from '@narrative/shared';
import { TribunalAppealsBaseListComponent } from './TribunalAppealsBaseList';
import { withPaginationController } from '../../../shared/containers/withPaginationController';

export const TribunalAppealsUnderReviewListPage = compose(
  withPaginationController<WithAllNichesUnderTribunalReviewProps>(
    withAllNichesUnderTribunalReview,
    WebRoute.AppealsUnderReview
  )
)(TribunalAppealsBaseListComponent) as React.ComponentClass<{}>;
