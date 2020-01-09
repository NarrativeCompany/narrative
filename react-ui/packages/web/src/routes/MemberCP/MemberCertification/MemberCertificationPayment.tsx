import * as React from 'react';
import { compose } from 'recompose';
import { RouteComponentProps, withRouter } from 'react-router';
import { InjectedIntlProps, injectIntl } from 'react-intl';
import { WithUserKycProps } from './MemberCertificationDetails';
import { Paragraph } from '../../../shared/components/Paragraph';
import { ImmediateFiatPaymentButton } from '../../../shared/components/invoice/ImmediateFiatPaymentButton';
import { WebRoute } from '../../../shared/constants/routes';
import { MemberCertificationMessages } from '../../../shared/i18n/MemberCertificationMessages';
import { InvoiceType, UserKycStatus } from '@narrative/shared';
import { EnhancedUserKycStatus } from '../../../shared/enhancedEnums/userKycStatus';

type Props =
  WithUserKycProps &
  InjectedIntlProps &
  RouteComponentProps<{}>;

const MemberCertificationPaymentComponent: React.SFC<Props> = (props) => {
  const {
    userKyc: { kycStatus, payPalCheckoutDetails, kycPricing },
    intl: { formatMessage },
    history
  } = props;

  if (!payPalCheckoutDetails) {
    // todo:error-handling: This should never happen, so log something with the server and error out.
    return null;
  }

  const status = EnhancedUserKycStatus.get(kycStatus);
  const promoMessage = kycPricing && kycPricing.kycPromoMessage;
  const paymentDescriptionMessage = status.isNone() ?
    MemberCertificationMessages.CertificationPayment :
    MemberCertificationMessages.CertificationRetryPayment;

  return (
    <React.Fragment>
      {promoMessage && kycStatus === UserKycStatus.NONE &&
      <Paragraph color="success" size="large" style={{marginBottom: '15px', fontWeight: 500}}>
        {promoMessage}
      </Paragraph>}

      <ImmediateFiatPaymentButton
        invoiceType={InvoiceType.KYC_CERTIFICATION}
        payPalCheckoutDetails={payPalCheckoutDetails}
        paymentDescription={formatMessage(paymentDescriptionMessage)}
        onSuccessfulPayment={() => history.push(WebRoute.MemberCertificationForm)}
      />
    </React.Fragment>
  );
};

export const MemberCertificationPayment = compose(
  injectIntl,
  withRouter
)(MemberCertificationPaymentComponent) as React.ComponentClass<WithUserKycProps>;
