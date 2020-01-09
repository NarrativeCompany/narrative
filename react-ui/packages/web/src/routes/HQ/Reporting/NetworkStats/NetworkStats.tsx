import * as React from 'react';
import { compose } from 'recompose';
import { FormattedMessage } from 'react-intl';
import { SEO } from '../../../../shared/components/SEO';
import { PageHeader } from '../../../../shared/components/PageHeader';
import { SEOMessages } from '../../../../shared/i18n/SEOMessages';
import { NetworkStatsMessages } from '../../../../shared/i18n/NetworkStatsMessages';
import { NetworkStatsSections } from './NetworkStatsSections';

const NetworkStatsComponent: React.SFC<{}> = () => {
  return (
    <React.Fragment>
      <SEO
        title={SEOMessages.NetworkStatsTitle}
        description={SEOMessages.NetworkStatsDescription}
      />

      <PageHeader
        iconType="network-stats"
        preTitle={<FormattedMessage {...NetworkStatsMessages.PageHeaderPreTitle}/>}
        title={<FormattedMessage {...NetworkStatsMessages.PageHeaderTitle}/>}
      />

      <NetworkStatsSections />

    </React.Fragment>
  );
};

export default compose(
)(NetworkStatsComponent) as React.ComponentClass<{}>;
