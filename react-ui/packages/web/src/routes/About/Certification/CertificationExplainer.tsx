import * as React from 'react';
import { compose } from 'recompose';
import { ViewWrapper } from '../../../shared/components/ViewWrapper';
import { DetailsGradientBox } from '../../../shared/components/DetailsGradientBox';
import { AboutPageHeader } from '../components/AboutPageHeader';
import { FormattedMessage, InjectedIntlProps, injectIntl } from 'react-intl';
import { CertificationExplainerMessages } from '../../../shared/i18n/CertificationExplainerMessages';
import { CertificationExplainerDetails } from './CertificationExplainerDetails';

const CertificationExplainerComponent: React.SFC<InjectedIntlProps> = (props) => {
  const { intl: { formatMessage } } = props;

  return (
    <ViewWrapper
      maxWidth={890}
      style={{ paddingTop: 75 }}
      gradientBox={<DetailsGradientBox color="darkBlue" size="large"/>}
    >
      <AboutPageHeader
        seoTitle={formatMessage(CertificationExplainerMessages.SeoTitle)}
        seoDescription={formatMessage(CertificationExplainerMessages.SeoDescription)}
        title={<FormattedMessage {...CertificationExplainerMessages.PageHeaderTitle}/>}
        description={<FormattedMessage {...CertificationExplainerMessages.PageHeaderDescription}/>}
      />

      <CertificationExplainerDetails />

    </ViewWrapper>
  );
};

export default compose(
  injectIntl,
)(CertificationExplainerComponent) as React.ComponentClass<{}>;
