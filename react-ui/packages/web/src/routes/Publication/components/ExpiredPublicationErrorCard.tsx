import * as React from 'react';
import { CardContainer } from '../Details/Invitation/PublicationInvitation';
import { PublicationDetailsMessages } from '../../../shared/i18n/PublicationDetailsMessages';
import { Card } from '../../../shared/components/Card';
import { SEO } from '../../../shared/components/SEO';
import { ExpiredPublicationError, ExpiredPublicationErrorProps } from './ExpiredPublicationError';

export const ExpiredPublicationErrorCard: React.SFC<ExpiredPublicationErrorProps> = (props) => {
  const { publication } = props;

  return (
    <React.Fragment>
      <SEO title={PublicationDetailsMessages.ExpiredSeoTitle} publication={publication} />

      <CardContainer>
        <Card>
          <ExpiredPublicationError {...props} />
        </Card>
      </CardContainer>
    </React.Fragment>
  );
};
