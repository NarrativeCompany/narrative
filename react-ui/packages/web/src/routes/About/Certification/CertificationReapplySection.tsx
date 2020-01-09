import * as React from 'react';
import { WithLoadedKycPricingProps } from '@narrative/shared';
import { AboutSection } from '../components/AboutSection';
import { CertificationExplainerMessages } from '../../../shared/i18n/CertificationExplainerMessages';
import { ContainedLoading } from '../../../shared/components/Loading';

export const CertificationReapplySection: React.SFC<WithLoadedKycPricingProps> = (props) => {
  const { kycPricing, loading } = props;

  if (loading) {
    return (
      <AboutSection title={CertificationExplainerMessages.CanIReapply}>
        <ContainedLoading />
      </AboutSection>
    );
  }

  const { retryPrice } = kycPricing;

  return (
    <AboutSection
      title={CertificationExplainerMessages.CanIReapply}
      message={CertificationExplainerMessages.CanIReapplyDescription}
      messageValues={{retryPrice}}
    />
  );
};
