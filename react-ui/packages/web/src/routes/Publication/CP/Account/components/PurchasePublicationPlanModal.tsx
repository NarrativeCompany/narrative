import * as React from 'react';
import {
  PublicationPlanType,
  Publication,
  PublicationPlanDetail
} from '@narrative/shared';
import { Modal } from 'antd';
import { OnPlanPurchasedHandler } from '../PublicationAccount';
import { PurchasePublicationPlanForm } from './PurchasePublicationPlanForm';
import { EnhancedPublicationPlanType } from '../../../../../shared/enhancedEnums/publicationPlanType';
import { FormattedMessage } from 'react-intl';
import { PublicationDetailsMessages } from '../../../../../shared/i18n/PublicationDetailsMessages';
import { ContainedLoading } from '../../../../../shared/components/Loading';

/*
  jw: this component is minimal on purpose!  We want to ensure that the state of the invoice process is lost once the
      modal is closed, so if we have state defined here it wont. Hence: 'PurchasePublicationPlanModalBody'
 */

export interface PurchasePublicationPlanModalProps extends OnPlanPurchasedHandler {
  publication: Publication;
  planDetails: PublicationPlanDetail;
  plan?: PublicationPlanType;
  visible?: boolean;
  dismiss: () => void;
}

export const PurchasePublicationPlanModal: React.SFC<PurchasePublicationPlanModalProps> = (props) => {
  const { visible, dismiss, plan, ...rest } = props;
  const { planDetails } = rest;

  // jw: if a plan has not been selected yet then just short out with a loading spinner in the modal
  if (!plan) {
    return (
      <Modal>
        <ContainedLoading/>
      </Modal>
    );
  }

  const currentPlanType = EnhancedPublicationPlanType.get(planDetails.plan);
  const planType = EnhancedPublicationPlanType.get(plan);
  const planName = <FormattedMessage {...planType.name} />;

  let titleMessage: FormattedMessage.MessageDescriptor;
  if (planDetails.withinTrialPeriod) {
    titleMessage = PublicationDetailsMessages.ActivatePlanModalTitle;

  } else if (currentPlanType === planType) {
    titleMessage = PublicationDetailsMessages.DowngradeToPlanModalTitle;

  } else if (currentPlanType.isBasicPlan()) {
    titleMessage = PublicationDetailsMessages.UpgradeToPlanModalTitle;

  } else {
    titleMessage = PublicationDetailsMessages.RenewPlanModalTitle;
  }

  return (
    <Modal
      visible={visible}
      onCancel={props.dismiss}
      footer={null}
      destroyOnClose={true}
      width={700}
      title={<FormattedMessage {...titleMessage} values={{planName}} />}
    >
      <PurchasePublicationPlanForm
        {...rest}
        plan={plan}
        dismissModal={dismiss}
      />
    </Modal>
  );
};
