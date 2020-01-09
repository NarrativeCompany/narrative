import * as React from 'react';
import { OpenPlanPurchaseModalHandler } from '../PublicationAccount';
import { PublicationPlanDetail, Publication, PublicationPlanType } from '@narrative/shared';
import { PurchasePublicationPlanSection } from './PurchasePublicationPlanSection';
import { PublicationDetailsMessages } from '../../../../../shared/i18n/PublicationDetailsMessages';
import { EnhancedPublicationStatus } from '../../../../../shared/enhancedEnums/publicationStatus';
import { EnhancedPublicationPlanType } from '../../../../../shared/enhancedEnums/publicationPlanType';
import { LocalizedTime } from '../../../../../shared/components/LocalizedTime';
import { FormattedMessage } from 'react-intl';
import { LocalizedNumber } from '../../../../../shared/components/LocalizedNumber';
import { Button } from '../../../../../shared/components/Button';
import { Section } from '../../../../../shared/components/Section';

interface Props extends OpenPlanPurchaseModalHandler {
  publication: Publication;
  planDetails: PublicationPlanDetail;
}

export const RenewPublicationPlanSection: React.SFC<Props> = (props) => {
  const {
    publication: { status },
    planDetails: { plan, withinTrialPeriod, endDatetime, deletionDatetime },
    openPurchasePlanModal
  } = props;

  const planType = EnhancedPublicationPlanType.get(plan);
  const planName = <FormattedMessage {...planType.name} />;
  const statusType = EnhancedPublicationStatus.get(status);
  const isExpired = statusType.isExpired();
  const endDate = <LocalizedTime time={endDatetime} dateOnly={true} />;
  const deletionDate = <LocalizedTime time={deletionDatetime} dateOnly={true} />;

  // jw: this feels really wonky, but because the compiler things this is shadowed if it is defined below the if block
  //     below when that if block has its own definitions I am forced to initialize this up top with the  ideal values.
  let title = PublicationDetailsMessages.RenewPlanDueNowTitle;
  let description = PublicationDetailsMessages.RenewPlanDueNowDescription;

  const descriptionValues = isExpired
    ? {endDate, deletionDate}
    : {endDate};

  // jw: if the publication is within the trial period then this is quite a bit simpler:
  if (withinTrialPeriod) {
    // todo:error-handling: we should assert that the plan is BASIC, since that is the only available plan for trials.

    title = PublicationDetailsMessages.RenewPlanTrialTitle;
    description = PublicationDetailsMessages.RenewPlanTrialDescription;
    if (isExpired) {
      title = PublicationDetailsMessages.RenewPlanTrialPastDueTitle;
      description = PublicationDetailsMessages.RenewPlanTrialPastDueDescription;
    }
    return (
      <PurchasePublicationPlanSection
        title={title}
        description={description}
        descriptionValues={descriptionValues}
        buttonType="primary"
        buttonText={PublicationDetailsMessages.ActivateButtonText}
        buttonTextValues={{planName}}
        plan={plan}
        openPurchasePlanModal={openPurchasePlanModal}
      />
    );
  }

  const { withinRenewalPeriod } = props.planDetails;

  // jw: if the site is not within the renewal window then let's output a simple section with no button.
  if (!withinRenewalPeriod) {
    return (
      <Section
        title={<FormattedMessage {...PublicationDetailsMessages.RenewPlanRenewalTitle} />}
        description={<FormattedMessage
          {...PublicationDetailsMessages.RenewPlanRenewalDescription}
          values={descriptionValues}
        />}
      />
    );
  }

  if (isExpired) {
    title = PublicationDetailsMessages.RenewPlanTrialPastDueTitle;
    description = PublicationDetailsMessages.RenewPlanTrialPastDueDescription;
  }

  // jw: if the site is currently on business we need to include either a button to downgrade, or an explination of why
  //     they cannot if they are above any limits.
  let afterButton: React.ReactNode | undefined;
  if (planType.isBusinessPlan()) {
    const basicPlanType = EnhancedPublicationPlanType.get(PublicationPlanType.BASIC);
    const { maxEditors, maxWriters } = basicPlanType;
    const { editors, writers } = props.planDetails;

    if (editors > maxEditors || writers > maxWriters) {
      const editorCount = <LocalizedNumber value={editors} />;
      const writerCount = <LocalizedNumber value={writers} />;

      afterButton = (
        <FormattedMessage
          {...PublicationDetailsMessages.CannotDowngradeWarning}
          values={{writers, writerCount, editors, editorCount}}
        />
      );
    } else {
      const basicPlanName = <FormattedMessage {...basicPlanType.name} />;

      afterButton = (
        <Button
          type="default"
          size="small"
          onClick={() => openPurchasePlanModal(PublicationPlanType.BASIC)}
        >
          <FormattedMessage {...PublicationDetailsMessages.DowngradeButtonText} values={{basicPlanName}} />
        </Button>
      );
    }
  }
  return (
    <PurchasePublicationPlanSection
      title={title}
      description={description}
      descriptionValues={descriptionValues}
      buttonType="primary"
      buttonText={PublicationDetailsMessages.RenewButtonText}
      plan={plan}
      openPurchasePlanModal={openPurchasePlanModal}
      afterButton={afterButton}
    />
  );
};
