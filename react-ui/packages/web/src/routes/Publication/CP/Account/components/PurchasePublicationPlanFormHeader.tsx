import * as React from 'react';
import { PublicationInvoice, PublicationPlanDetail } from '@narrative/shared';
import { Paragraph } from '../../../../../shared/components/Paragraph';
import { PublicationDetailsMessages } from '../../../../../shared/i18n/PublicationDetailsMessages';
import { NrveValue } from '../../../../../shared/components/rewards/NrveValue';
import { EnhancedPublicationPlanType } from '../../../../../shared/enhancedEnums/publicationPlanType';
import { FormattedMessage, MessageValue } from 'react-intl';
import { LocalizedTime } from '../../../../../shared/components/LocalizedTime';
import { NRVE } from '../../../../../shared/components/NRVE';
import { USD } from '../../../../../shared/components/USD';
import { Link } from '../../../../../shared/components/Link';
import { generatePath } from 'react-router';
import { WebRoute } from '../../../../../shared/constants/routes';
import { compose } from 'recompose';
import {
  WithCurrentUserProps,
  withExtractedCurrentUser
} from '../../../../../shared/containers/withExtractedCurrentUser';
import { Alert } from 'antd';

interface ParentProps {
  publicationInvoice: PublicationInvoice;
  planDetails: PublicationPlanDetail;
}

type Props = ParentProps &
  WithCurrentUserProps;

const PurchasePublicationPlanFormDescriptionComponent: React.SFC<Props> = (props) => {
  const {
    publicationInvoice: {
      estimatedRefundAmount,
      newEndDatetime,
      invoiceDetail: {
        nrveAmount,
        fiatPayment
      }
    },
    planDetails
  } = props;

  if (!nrveAmount || !fiatPayment) {
    // todo:error-handling: The fiatPayment should always be present since we create it as part of creating the invoice.
    return null;
  }

  const newPlanType = EnhancedPublicationPlanType.get(props.publicationInvoice.plan);
  const currentPlanType = EnhancedPublicationPlanType.get(planDetails.plan);

  const newPlanName = <FormattedMessage {...newPlanType.name} />;
  const currentPlanName = <FormattedMessage {...currentPlanType.name} />;
  const newEndDate = <LocalizedTime time={newEndDatetime} dateOnly={true} />;
  const nrvePrice = <NRVE amount={nrveAmount} aboutNrveTarget="_blank" />;
  const totalUsdPrice = <USD value={fiatPayment.totalUsdAmount} />;

  let values: {[key: string]: MessageValue | JSX.Element} = { newPlanName, newEndDate, nrvePrice, totalUsdPrice };
  let description;
  // jw: if this is a straight up renewal then let's give one message
  if (newPlanType === currentPlanType) {
    description = planDetails.withinTrialPeriod
      ? PublicationDetailsMessages.PurchasePlanFormTrialDescription
      : PublicationDetailsMessages.PurchasePlanFormRenewalDescription;

  } else if (estimatedRefundAmount) {
    const { currentUser: { username } } = props;

    // jw: we only give refunds during plan changes
    const refundAmount = <NrveValue value={estimatedRefundAmount}/>;
    const rewardPointsLink = (
      <Link to={generatePath(WebRoute.UserProfileRewards, { username })} target="_blank">
        <FormattedMessage {...PublicationDetailsMessages.RewardPointsLinkText}/>
      </Link>
    );

    values = { ...values, currentPlanName, refundAmount, rewardPointsLink };
    description = PublicationDetailsMessages.PurchasePlanFormUpgradeWithRefundDescription;

  } else {
    values = {...values, currentPlanName};
    // jw: since we only have two plan types and we know that the plan is changing as part of this invoice we can base
    //     the upgrade -vs- downgrade wording off of just the plan we are switching to. Nice!
    description = newPlanType.isBasicPlan()
      ? PublicationDetailsMessages.PurchasePlanFormDowngradeDescription
      : PublicationDetailsMessages.PurchasePlanFormUpgradeDescription;
  }

  return (
    <React.Fragment>
      <Paragraph marginBottom="large">
        <FormattedMessage {...description} values={values} />
      </Paragraph>
      {planDetails.eligibleForDiscount &&
        <Alert
          type="warning"
          message={<FormattedMessage {...PublicationDetailsMessages.PurchasePlanDiscountDescription}/>}
          style={{marginBottom: 15}}
        />
      }
    </React.Fragment>
  );
};

export const PurchasePublicationPlanFormDescription = compose(
  withExtractedCurrentUser
)(PurchasePublicationPlanFormDescriptionComponent) as React.ComponentClass<ParentProps>;
