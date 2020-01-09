import * as React from 'react';
import { compose } from 'recompose';
import { WebRoute } from '../../../shared/constants/routes';
import {
  withAllMyQueueTribunalReview,
  WithAllMyQueueTribunalReviewProps
} from '@narrative/shared';
import { TribunalAppealsBaseListComponent } from './TribunalAppealsBaseList';
import { withPaginationController } from '../../../shared/containers/withPaginationController';

export const TribunalAppealsMyQueueListPage = compose(
  withPaginationController<WithAllMyQueueTribunalReviewProps>(
    withAllMyQueueTribunalReview,
    WebRoute.AppealsMyQueue
  )
)(TribunalAppealsBaseListComponent) as React.ComponentClass<{}>;
