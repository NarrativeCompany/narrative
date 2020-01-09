import * as React from 'react';
import { OpenPlanPurchaseModalHandler } from '../PublicationAccount';
import { PublicationPlanDetail, PublicationPlanType } from '@narrative/shared';
import { EnhancedPublicationPlanType } from '../../../../../shared/enhancedEnums/publicationPlanType';
import { PurchasePublicationPlanSection } from './PurchasePublicationPlanSection';
import { FormattedMessage } from 'react-intl';
import { PublicationDetailsMessages } from '../../../../../shared/i18n/PublicationDetailsMessages';

interface Props extends OpenPlanPurchaseModalHandler {
  planDetails: PublicationPlanDetail;
}

export const UpgradePublicationPlanSection: React.SFC<Props> = (props) => {
  const { planDetails: { plan, withinTrialPeriod },  openPurchasePlanModal } = props;

  const planType = EnhancedPublicationPlanType.get(plan);

  // jw: if the site is already on a business plan then there is no reason to include the upgrade section.
  if (planType.isBusinessPlan()) {
    return null;
  }

  const businessPlanType = EnhancedPublicationPlanType.get(PublicationPlanType.BUSINESS);

  const businessPlanName = <FormattedMessage {...businessPlanType.name} />;

  return (
    <PurchasePublicationPlanSection
      title={withinTrialPeriod
        ? PublicationDetailsMessages.UpgradeSectionTitleWithinTrial
        : PublicationDetailsMessages.UpgradeSectionTitle
      }
      description={withinTrialPeriod
        ? PublicationDetailsMessages.UpgradeSectionDescriptionWithinTrial
        : PublicationDetailsMessages.UpgradeSectionDescription
      }
      buttonType="green"
      buttonText={withinTrialPeriod
        ? PublicationDetailsMessages.ActivateButtonText
        : PublicationDetailsMessages.UpgradeButtonText
      }
      buttonTextValues={{planName: businessPlanName}}
      plan={PublicationPlanType.BUSINESS}
      openPurchasePlanModal={openPurchasePlanModal}
    />
  );
};
