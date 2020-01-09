import * as React from 'react';
import { compose } from 'recompose';
import { SEO } from '../../../../shared/components/SEO';
import { PublicationDetailsMessages } from '../../../../shared/i18n/PublicationDetailsMessages';
import {
  WithPublicationDetailsContextProps
} from '../../components/PublicationDetailsContext';
import { PowerUsersBody } from './components/PowerUsersBody';
import { withExpiredPublicationError } from '../../components/withExpiredPublicationError';

type Props = WithPublicationDetailsContextProps;

const PublicationPowerUsersComponent: React.SFC<Props> = (props) => {
  const { publicationDetail: { publication } } = props;

  return (
    <React.Fragment>
      <SEO title={PublicationDetailsMessages.PowerUsers} publication={publication} />
      <PowerUsersBody {...props} />
    </React.Fragment>
  );
};

export default compose(
  withExpiredPublicationError(true)
)(PublicationPowerUsersComponent) as React.ComponentClass<{}>;
