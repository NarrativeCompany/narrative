import * as React from 'react';
import { compose, withProps } from 'recompose';
import { FormattedMessage } from 'react-intl';
import { MemberCertificationPayment } from './MemberCertificationPayment';
import { MemberCertificationStep } from './MemberCertificationStep';
import { WithUserKycProps } from './MemberCertificationDetails';
import { MemberCertificationMessages } from '../../../shared/i18n/MemberCertificationMessages';
import { EnhancedUserKycStatus } from '../../../shared/enhancedEnums/userKycStatus';
import { Paragraph } from '../../../shared/components/Paragraph';

interface WithProps {
  price: string;
  feeTypeMessage: FormattedMessage.MessageDescriptor;
  description?: FormattedMessage.MessageDescriptor;
  showPaymentButton: boolean;
}

type Props =
  WithUserKycProps &
  WithProps;

export const MemberCertificationStepOneComponent: React.SFC<Props> = (props) => {
  const { price, description, feeTypeMessage, showPaymentButton } = props;

  const feeType = <FormattedMessage {...feeTypeMessage}/>;
  const title =
    <FormattedMessage {...MemberCertificationMessages.CertificationStepOneTitle} values={{ price, feeType }}/>;

  return (
    <MemberCertificationStep
      title={title}
      description={description &&
        <Paragraph>
          <FormattedMessage {...description} />
        </Paragraph>
      }
      isComplete={!showPaymentButton}
    >
      {showPaymentButton &&
      <MemberCertificationPayment {...props}/>}
    </MemberCertificationStep>
  );
};

export const MemberCertificationStepOne = compose(
  withProps((props: WithUserKycProps) => {
    const { userKyc } = props;

    const status = EnhancedUserKycStatus.get(userKyc.kycStatus);

    const price =
      userKyc.payPalCheckoutDetails &&
      userKyc.payPalCheckoutDetails.usdAmount;

    const feeTypeMessage = status.isRejected() ?
      MemberCertificationMessages.ResubmissionFee :
      MemberCertificationMessages.ApplicationFee;
    const description = status.isRejected() ?
      MemberCertificationMessages.CertificationStepOneRejectedDescription :
      undefined;
    const showPaymentButton = (status.isSupportsPayments() && userKyc.payPalCheckoutDetails);

    return { price, feeTypeMessage, description, showPaymentButton };
  })
)(MemberCertificationStepOneComponent) as React.ComponentClass<WithUserKycProps>;
