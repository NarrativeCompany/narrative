import * as React from 'react';
import { InvoiceSummaryGrid } from './InvoiceSummaryGrid';
import {
  InvoiceDetail,
  InvoiceStatus,
  withState,
  WithStateProps
} from '@narrative/shared';
import { InvoicePaymentOptions } from './InvoicePaymentOptions';
import { InvoiceProps } from './AuctionInvoice';
import { compose, withHandlers } from 'recompose';

export interface HandleInvoiceUpdateCallback {
  // tslint:disable-next-line no-any
  handleInvoiceUpdate: (newInvoice: InvoiceDetail ) => any;
}

type WithProps = WithStateProps<InvoiceProps> &
  HandleInvoiceUpdateCallback;

const InvoiceDetailsComponent: React.SFC<WithProps> = (props) => {
  const { invoice } = props.state;

  return (
    <React.Fragment>
      <InvoiceSummaryGrid invoice={invoice} />
      {invoice.status === InvoiceStatus.INVOICED
        && <InvoicePaymentOptions
        invoice={invoice}
        handleInvoiceUpdate={props.handleInvoiceUpdate}
      />}
    </React.Fragment>
  );
};

export const InvoiceDetails = compose(
  withState((props: InvoiceProps) => {
    const { invoice } = props;
    return { invoice };
  }),
  withHandlers({
    handleInvoiceUpdate: (props: WithStateProps<InvoiceProps>) => async (newInvoice: InvoiceDetail) => {
        props.setState(ss => ({
          ...ss,
          invoice: newInvoice
        }));
      }
  })
)(InvoiceDetailsComponent) as React.ComponentClass<InvoiceProps>;
