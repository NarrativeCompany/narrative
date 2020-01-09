import * as React from 'react';
import { compose, withHandlers } from 'recompose';
import {
  FiatPaymentProcessorType,
  InvoiceType,
  PayPalCheckoutDetails,
  withPostImmediateFiatPayment,
  WithPostImmediateFiatPaymentProps
} from '@narrative/shared';
import { withFiatPaymentButton, WithFiatPaymentButtonProps } from '../../containers/withFiatPaymentButton';
import { PayWithPayPalButton } from './PayWithPayPalButton';

interface ParentProps {
  invoiceType: InvoiceType;
  payPalCheckoutDetails: PayPalCheckoutDetails;
  paymentDescription: string;
  onSuccessfulPayment: () => void;
}

type Props =
  ParentProps &
  WithFiatPaymentButtonProps;

const ImmediateFiatPaymentButtonComponent: React.SFC<Props> = (props) => {
  const { payPalCheckoutDetails, processingPayment, onFiatPaymentToken, paymentDescription } = props;

  return (
    <PayWithPayPalButton
      checkoutDetails={payPalCheckoutDetails}
      paymentDescription={paymentDescription}
      handlePaymentId={(paymentToken: string) => onFiatPaymentToken(paymentToken, FiatPaymentProcessorType.PAYPAL)}
      processingPayment={processingPayment}
    />
  );
};

export const ImmediateFiatPaymentButton = compose(
  withPostImmediateFiatPayment,
  withHandlers({
    processFiatPaymentToken: (props: WithPostImmediateFiatPaymentProps & ParentProps) =>
      async (paymentToken: string, processorType: FiatPaymentProcessorType) =>
    {
      const { postImmediateFiatPayment, invoiceType, onSuccessfulPayment } = props;

      // jw: it is vital that we await the response from this query.
      const invoice = await postImmediateFiatPayment({ invoiceType, processorType, paymentToken });

      // jw: we were successful if we got a invoice.
      const success = invoice !== undefined && invoice !== null;

      if (success) {
        onSuccessfulPayment();
      }

      return success;
    }
  }),
  withFiatPaymentButton
)(ImmediateFiatPaymentButtonComponent) as React.ComponentClass<ParentProps>;
