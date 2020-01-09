import * as React from 'react';
import { InvoiceStatus } from '@narrative/shared';
import { InvoiceMessages } from '../../../../shared/i18n/InvoiceMessages';
import { FormattedMessage } from 'react-intl';
import { SummaryGridRow } from '../../../../shared/components/SummaryGridRow';
import { CountDown } from '../../../../shared/components/CountDown';
import { LocalizedTime } from '../../../../shared/components/LocalizedTime';
import { InvoiceProps } from './AuctionInvoice';

export const InvoiceSummaryGridActivityDatetimeRow: React.SFC<InvoiceProps> = (props) => {
  const { invoice } = props;

  if (invoice.status === InvoiceStatus.INVOICED) {
    return (
      <SummaryGridRow title={<FormattedMessage {...InvoiceMessages.PaymentDueBy}/>}>
        <CountDown endTime={invoice.paymentDueDatetime}/>
      </SummaryGridRow>
    );
  }

  if (invoice.status === InvoiceStatus.PAID) {
    const payment = invoice.nrvePayment ? invoice.nrvePayment : invoice.fiatPayment;
    if (!payment) {
      // todo:error-handling: we should log a message to the server that there is invoice marked as paid that does
      //      not actually have a payment associated to it. Not good!
      return null;
    }

    const { transactionDate } = payment;
    if (!transactionDate) {
      // todo:error-handling: Similar to above, so the invoice has a payment, but that payment does not have a
      //      transactionDatetime. Still not good!
      return null;
    }

    return (
      <SummaryGridRow title={<FormattedMessage {...InvoiceMessages.Paid}/>}>
        <LocalizedTime time={transactionDate}/>
      </SummaryGridRow>
    );
  }

  let title;
  switch (invoice.status) {
    case InvoiceStatus.EXPIRED:
      title = InvoiceMessages.Expired;
      break;
    case InvoiceStatus.CHARGEBACK:
      title = InvoiceMessages.Chargeback;
      break;
    case InvoiceStatus.REFUNDED:
      title = InvoiceMessages.Refunded;
      break;
    case InvoiceStatus.CANCELED:
      title = InvoiceMessages.Canceled;
      break;
    default:
      // todo:error-handling: We should never get here, so if we do let's log the unexpected status with the server
      return null;
  }

  if (!invoice.updateDatetime) {
    // todo:error-handling: We should always have a updateDatetime in this case
    return null;
  }

  return (
    <SummaryGridRow title={<FormattedMessage {...title}/>}>
      <LocalizedTime time={invoice.updateDatetime}/>
    </SummaryGridRow>
  );

};
