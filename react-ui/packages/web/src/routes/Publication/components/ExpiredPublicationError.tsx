import * as React from 'react';
import { Publication } from '@narrative/shared';
import { LocalizedTime } from '../../../shared/components/LocalizedTime';
import { PublicationDetailsMessages } from '../../../shared/i18n/PublicationDetailsMessages';
import { Link } from '../../../shared/components/Link';
import { getIdForUrl } from '../../../shared/utils/routeUtils';
import { WebRoute } from '../../../shared/constants/routes';
import { generatePath } from 'react-router';
import { FormattedMessage } from 'react-intl';
import { Paragraph } from '../../../shared/components/Paragraph';
import { Heading } from '../../../shared/components/Heading';

export interface ExpiredPublicationErrorProps {
  publication: Publication;
  owner: boolean;
  deletionDatetime: string;
}

export const ExpiredPublicationError: React.SFC<ExpiredPublicationErrorProps> = (props) => {
  const { publication, owner, deletionDatetime } = props;

  const deletionDate = <LocalizedTime time={deletionDatetime} dateOnly={true} />;

  // jw: for expired publications the owner can re-activate the publication by renewing their plan.
  let ownerMessage: React.ReactNode | undefined;
  if (owner) {
    const id = getIdForUrl(publication.prettyUrlString, publication.oid);
    const payTheFee = (
      <Link to={generatePath(WebRoute.PublicationAccount, {id})}>
        <FormattedMessage {...PublicationDetailsMessages.PayTheFeeText}/>;
      </Link>
    );

    ownerMessage = (
      <FormattedMessage
        {...PublicationDetailsMessages.ReactivatePublicationMessage}
        values={{payTheFee}}
      />
    );
  }

  return (
    <React.Fragment>
      <Heading size={2}>
        <FormattedMessage {...PublicationDetailsMessages.CurrentlyUnavailable} />
      </Heading>

      <Paragraph marginBottom={ownerMessage ? 'large' : undefined}>
        <FormattedMessage
          {...PublicationDetailsMessages.PublicationExpiredMessage}
          values={{deletionDate}}
        />
      </Paragraph>

      {ownerMessage &&
        <Paragraph>
          {ownerMessage}
        </Paragraph>
      }
    </React.Fragment>
  );
};
