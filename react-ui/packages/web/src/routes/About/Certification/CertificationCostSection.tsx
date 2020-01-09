import * as React from 'react';
import { WithLoadedKycPricingProps } from '@narrative/shared';
import { AboutSection } from '../components/AboutSection';
import { CertificationExplainerMessages } from '../../../shared/i18n/CertificationExplainerMessages';
import { ContainedLoading } from '../../../shared/components/Loading';

export const CertificationCostSection: React.SFC<WithLoadedKycPricingProps> = (props) => {
  const { loading, kycPricing } = props;

  if (loading) {
    return (
      <AboutSection title={CertificationExplainerMessages.HowMuchDoesCertificationCost}>
        <ContainedLoading />
      </AboutSection>
    );
  }

  const { initialPrice, kycPromoPrice } = kycPricing;

  if (kycPricing.kycPromoPrice) {
    return (
      <AboutSection
        title={CertificationExplainerMessages.HowMuchDoesCertificationCost}
        message={CertificationExplainerMessages.HowMuchDoesCertificationCostDescriptionWithPromo}
        messageValues={{initialPrice, kycPromoPrice}}
      />
    );
  }

  return (
    <AboutSection
      title={CertificationExplainerMessages.HowMuchDoesCertificationCost}
      message={CertificationExplainerMessages.HowMuchDoesCertificationCostDescription}
      messageValues={{initialPrice}}
    />
  );
};
