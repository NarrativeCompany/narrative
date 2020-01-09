import * as React from 'react';
import { compose } from 'recompose';
import { WithPublicationDetailsContextProps } from '../../components/PublicationDetailsContext';
import { ReviewQueueBody } from './components/ReviewQueueBody';
import { SEO } from '../../../../shared/components/SEO';
import { PublicationDetailsMessages } from '../../../../shared/i18n/PublicationDetailsMessages';
import { withExpiredPublicationError } from '../../components/withExpiredPublicationError';

const PublicationReviewQueueComponent: React.SFC<WithPublicationDetailsContextProps> = (props) => {
  const { publicationDetail: { publication } } = props;

  return (
    <React.Fragment>
      <SEO title={PublicationDetailsMessages.ReviewQueueSeoTitle} publication={publication} />

      <ReviewQueueBody publication={publication} />
    </React.Fragment>
  );
};

export default compose(
  withExpiredPublicationError()
)(PublicationReviewQueueComponent) as React.ComponentClass<{}>;
