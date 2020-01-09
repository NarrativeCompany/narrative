import * as React from 'react';
import { compose } from 'recompose';
import {
  withKycPricing,
  WithLoadedKycPricingProps
} from '@narrative/shared';
import {
  withExtractedCurrentUser,
  WithExtractedCurrentUserProps
} from '../../../shared/containers/withExtractedCurrentUser';
import { FormattedMessage } from 'react-intl';
import { CertificationExplainerMessages } from '../../../shared/i18n/CertificationExplainerMessages';
import { AboutSection } from '../components/AboutSection';
import { AboutSectionParagraph } from '../components/AboutSectionParagraph';
import { CertificationCostSection } from './CertificationCostSection';
import { geeksOnly } from '../NRVE/NRVEExplainer';
import { CertificationReapplySection } from './CertificationReapplySection';
import { GetCertifiedCta } from './GetCertifiedCta';

type Props =
  WithExtractedCurrentUserProps &
  WithLoadedKycPricingProps;

const CertificationExplainerDetailsComponent: React.SFC<Props> = (props) => {
  const { kycPricing, loading, currentUser } = props;

  const username = currentUser
    ? currentUser.username
    : <FormattedMessage {...CertificationExplainerMessages.UsernamePlaceholder} />;

  return (
    <React.Fragment>
      <AboutSectionParagraph
        message={CertificationExplainerMessages.Description}
        values={{username}}
        style={{marginBottom: '60px'}}
      />

      <GetCertifiedCta />

      <AboutSection
        title={CertificationExplainerMessages.IsCertificationRequired}
        titleType="certified"
        message={CertificationExplainerMessages.IsCertificationRequiredDescription}
      />
      <AboutSection
        title={CertificationExplainerMessages.WhyGetCertified}
        titleType="certified"
        message={CertificationExplainerMessages.WhyGetCertifiedDescription}
      >
        <AboutSectionParagraph asBlock={true}>
          <ul>
            <li><FormattedMessage {...CertificationExplainerMessages.WhyGetCertifiedPoint1}/></li>
            <li><FormattedMessage {...CertificationExplainerMessages.WhyGetCertifiedPoint2}/></li>
            <li><FormattedMessage {...CertificationExplainerMessages.WhyGetCertifiedPoint3}/></li>
          </ul>
          <FormattedMessage {...CertificationExplainerMessages.TaxReportingExplainer}/>
        </AboutSectionParagraph>
      </AboutSection>

      <CertificationCostSection kycPricing={kycPricing} loading={loading} />

      <AboutSection
        title={CertificationExplainerMessages.WhatInformationIsNeeded}
        message={CertificationExplainerMessages.WhatInformationIsNeededDescription}
      />

      <AboutSection title={CertificationExplainerMessages.HowAreaYouProcessing}>
        <AboutSectionParagraph
          message={CertificationExplainerMessages.HowAreaYouProcessingDescription}
        />
        <AboutSectionParagraph message={CertificationExplainerMessages.GeeksOnlyDescription} values={{geeksOnly}}/>
      </AboutSection>

      <CertificationReapplySection kycPricing={kycPricing} loading={loading}/>

      <AboutSection
        title={CertificationExplainerMessages.MoreThanOneAccount}
        message={CertificationExplainerMessages.MoreThanOneAccountDescription}
      />
    </React.Fragment>
  );
};

export const CertificationExplainerDetails = compose(
  withExtractedCurrentUser,
  withKycPricing
)(CertificationExplainerDetailsComponent) as React.ComponentClass<{}>;
