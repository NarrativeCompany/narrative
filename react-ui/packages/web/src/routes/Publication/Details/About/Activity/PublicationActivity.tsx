import * as React from 'react';
import { compose } from 'recompose';
import {
  PublicationDetailsConnect,
  WithPublicationDetailsContextProps
} from '../../../components/PublicationDetailsContext';
import { PublicationDetailsMessages } from '../../../../../shared/i18n/PublicationDetailsMessages';
import { SEO } from '../../../../../shared/components/SEO';
import { PublicationLedgerEntries } from './components/PublicationLedgerEntries';

const PublicationActivityComponent: React.SFC<WithPublicationDetailsContextProps> = (props) => {
  const { publicationDetail: { publication } } = props;

  return (
    <React.Fragment>
      <SEO
        title={PublicationDetailsMessages.ActivitySeoTitle}
        publication={publication}
      />

      <PublicationLedgerEntries publication={publication} />
    </React.Fragment>
  );
};

export default compose(
  PublicationDetailsConnect
)(PublicationActivityComponent) as React.ComponentClass<{}>;
