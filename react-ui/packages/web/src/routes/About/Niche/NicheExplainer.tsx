import * as React from 'react';
import { FormattedMessage, injectIntl } from 'react-intl';
import { DetailsGradientBox } from '../../../shared/components/DetailsGradientBox';
import { ViewWrapper } from '../../../shared/components/ViewWrapper';
import { AboutPageHeader } from '../components/AboutPageHeader';
import { NicheExplainerMessages } from '../../../shared/i18n/NicheExplainerMessages';
import { AboutNichesSection } from './AboutNichesSection';
import { NicheOriginSection } from './NicheOriginSection';
import { NicheContentSubmissionSection } from './NicheContentSubmissionSection';
import { NicheControllersSection } from './NicheControllersSection';
import { BuyingNichesSection } from './BuyingNichesSection';
import { compose } from 'recompose';
import { InjectedIntlProps } from 'react-intl';
import { NicheSuggesterToBuySection } from './NicheSuggesterToBuySection';
import { BuySuggestedNicheSection } from './BuySuggestedNicheSection';
import { NicheOwnershipSection } from './NicheOwnershipSection';

const NicheExplainerComponent: React.SFC<InjectedIntlProps> = (props) => {
  const { intl: { formatMessage }} = props;

  const description = (
    <React.Fragment>
      <FormattedMessage {...NicheExplainerMessages.PageHeaderDescriptionOne}/>
      <br/>
      <FormattedMessage {...NicheExplainerMessages.PageHeaderDescriptionTwo}/>
    </React.Fragment>
  );

  return (
    <ViewWrapper
      maxWidth={890}
      style={{ paddingTop: 75 }}
      gradientBox={<DetailsGradientBox color="darkBlue" size="large"/>}
    >
      <AboutPageHeader
        seoTitle={formatMessage(NicheExplainerMessages.SEOTitle)}
        title={<FormattedMessage {...NicheExplainerMessages.PageHeaderTitle}/>}
        description={description}
      />

      <AboutNichesSection/>
      <NicheOriginSection/>
      <NicheContentSubmissionSection/>
      <NicheControllersSection/>
      <BuyingNichesSection/>
      <NicheSuggesterToBuySection/>
      <BuySuggestedNicheSection/>
      <NicheOwnershipSection/>
    </ViewWrapper>
  );
};

const NicheExplainer = compose(
  injectIntl
)(NicheExplainerComponent) as React.ComponentClass<{}>;

export default NicheExplainer;
