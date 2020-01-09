import * as React from 'react';
import { compose, withHandlers } from 'recompose';
import { InvoiceProps } from './AuctionInvoice';
import { HandleInvoiceUpdateCallback } from './InvoiceDetails';
import { InjectedIntlProps, injectIntl } from 'react-intl';

import {
  FiatPaymentProcessorType,
  withPutInvoiceFiatPayment,
  WithPutInvoiceFiatPaymentProps,
  InvoiceDetail
} from '@narrative/shared';
import { InvoiceMessages } from '../../../../shared/i18n/InvoiceMessages';
import { PayWithPayPalButton } from '../../../../shared/components/invoice/PayWithPayPalButton';
import { withFiatPaymentButton, WithFiatPaymentButtonProps } from '../../../../shared/containers/withFiatPaymentButton';

interface PaymentDataProps {
  paymentDescription?: string;
  customPaymentData?: string;
}

type ParentProps = InvoiceProps &
  HandleInvoiceUpdateCallback &
  PaymentDataProps;

type Props = InvoiceProps &
  InjectedIntlProps &
  WithFiatPaymentButtonProps &
  PaymentDataProps;

const InvoicePayWithCardButtonComponent: React.SFC<Props> = (props) => {
  const { invoice, intl: { formatMessage }, processingPayment, onFiatPaymentToken } = props;
  let { paymentDescription, customPaymentData } = props;

  const { fiatPayment } = invoice;

  if (!fiatPayment) {
    // todo:error-handling: we should report this unexpected case!
    return null;
  }

  const checkoutDetails = fiatPayment.payPalCheckoutDetails;

  if (!checkoutDetails) {
    // todo:error-handling: we should report this unexpected case!
    return null;
  }

  if (invoice.nicheAuctionInvoice) {
    const niche = invoice.nicheAuctionInvoice.auction.niche;
    const nicheName = niche.name;

    paymentDescription = formatMessage(InvoiceMessages.PayWithCardDescription, {nicheName});
    customPaymentData = `niche/${niche.oid}`;
  }

  if (!customPaymentData || !paymentDescription) {
    // todo:error-handling: We should always have payment details by the time we get here.
    return null;
  }

  return (
    <React.Fragment>
      <PayWithPayPalButton
        checkoutDetails={checkoutDetails}
        paymentDescription={paymentDescription}
        handlePaymentId={(paymentToken: string) => onFiatPaymentToken(paymentToken, FiatPaymentProcessorType.PAYPAL)}
        processingPayment={processingPayment}
        customPaymentData={customPaymentData}
      />
    </React.Fragment>
  );
};

export const InvoicePayWithCardButton = compose(
  withPutInvoiceFiatPayment,
  withHandlers({
    processFiatPaymentToken: (props: WithPutInvoiceFiatPaymentProps & ParentProps) =>
      async (paymentToken: string, processorType: FiatPaymentProcessorType) =>
    {
      const { putInvoiceFiatPayment, invoice, handleInvoiceUpdate } = props;

      // jw: it is vital that we await the response from this query.
      const paidInvoice: InvoiceDetail = await putInvoiceFiatPayment({processorType, paymentToken}, invoice.oid);

      if (paidInvoice) {
        handleInvoiceUpdate(paidInvoice);
        return true;
      }

      return false;
    }
  }),
  withFiatPaymentButton,
  injectIntl
)(InvoicePayWithCardButtonComponent) as React.ComponentClass<ParentProps>;
