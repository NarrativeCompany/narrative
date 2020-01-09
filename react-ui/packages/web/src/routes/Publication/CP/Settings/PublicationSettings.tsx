import * as React from 'react';
import { compose } from 'recompose';
import { WithPublicationDetailsContextProps } from '../../components/PublicationDetailsContext';
import { SEO } from '../../../../shared/components/SEO';
import { PublicationDetailsMessages } from '../../../../shared/i18n/PublicationDetailsMessages';
import { PublicationSettingsBody } from './components/PublicationSettingsBody';
import { withExpiredPublicationError } from '../../components/withExpiredPublicationError';

const PublicationSettingsComponent: React.SFC<WithPublicationDetailsContextProps> = (props) => {
  const { publicationDetail: { publication } } = props;

  return (
    <React.Fragment>
      <SEO title={PublicationDetailsMessages.PublicationSettingsSeoTitle} publication={publication} />

      <PublicationSettingsBody publication={publication} />
    </React.Fragment>
  );
};

export default compose(
  withExpiredPublicationError()
)(PublicationSettingsComponent) as React.ComponentClass<{}>;
