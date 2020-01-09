import * as React from 'react';
import { FormattedMessage, injectIntl } from 'react-intl';
import { ViewWrapper } from '../../../shared/components/ViewWrapper';
import { DetailsGradientBox } from '../../../shared/components/DetailsGradientBox';
import { AboutPageHeader } from '../components/AboutPageHeader';
import { AboutNRVESection } from './AboutNRVESection';
import { EarnNRVESection } from './EarnNRVESection';
import { NRVEUseInPlatformSection } from './NRVEUseInPlatformSection';
import { NRVEValueOutsidePlatformSection } from './NRVEValueOutsidePlatformSection';
import { BuyNRVESection } from './BuyNRVESection';
import { StoreNRVESection } from './StoreNRVESection';
import { NRVEExplainerMessages } from '../../../shared/i18n/NRVEExplainerMessages';
import { compose } from 'recompose';
import { InjectedIntlProps } from 'react-intl';

export const geeksOnly = (
  <strong>
    <FormattedMessage {...NRVEExplainerMessages.GeeksOnly}/>
  </strong>
);

const NRVEExplainerComponent: React.SFC<InjectedIntlProps> = (props) => {
  const { intl: { formatMessage }} = props;

  return (
    <ViewWrapper
      maxWidth={890}
      style={{ paddingTop: 75 }}
      gradientBox={<DetailsGradientBox color="darkBlue" size="large"/>}
    >
      <AboutPageHeader
        seoTitle={formatMessage(NRVEExplainerMessages.SEOTitle)}
        title={<FormattedMessage {...NRVEExplainerMessages.PageHeaderTitle}/>}
        description={<FormattedMessage {...NRVEExplainerMessages.PageHeaderDescription}/>}
      />

      <AboutNRVESection/>
      <EarnNRVESection/>
      <NRVEUseInPlatformSection/>
      <NRVEValueOutsidePlatformSection/>
      <BuyNRVESection/>
      <StoreNRVESection/>
    </ViewWrapper>
  );
};

const NRVEExplainer = compose(
  injectIntl
)(NRVEExplainerComponent) as React.ComponentClass<{}>;

export default NRVEExplainer;
