import * as React from 'react';
import { InvoiceProps } from './AuctionInvoice';
import { InvoicePayWithNrveOrCard } from './InvoicePayWithNrveOrCard';
import { InvoicePayWithNrve } from './InvoicePayWithNrve';
import { compose, withHandlers } from 'recompose';
import {
  withState,
  WithStateProps
} from '@narrative/shared';
import { InvoiceNrvePaymentProcessing } from './InvoiceNrvePaymentProcessing';
import { HandleInvoiceUpdateCallback } from './InvoiceDetails';

interface State {
  forcePayWithNrve: boolean;
}

export interface HandleForcePayWithNrveCallback {
  // tslint:disable-next-line no-any
  handleForcePayWithNrve: (forcePayWithNrve: boolean) => any;
}

export interface CardPaymentDataProps {
  cardPaymentDescription?: string;
  cardCustomPaymentData?: string;
}

type ParentProps =  InvoiceProps &
  HandleInvoiceUpdateCallback &
  CardPaymentDataProps;

type Props = ParentProps &
  WithStateProps<State> &
  HandleForcePayWithNrveCallback &
  CardPaymentDataProps;

const InvoicePaymentOptionsComponent: React.SFC<Props> = (props) => {
  const {
    handleInvoiceUpdate,
    handleForcePayWithNrve
  } = props;

  // jw: let's render from the state so that the invoice can be affected by Callbacks
  const { invoice } = props;
  const { nrvePayment, fiatPayment } = invoice;

  // jw: if we are invoiced, and have a payment object, then that means we are waiting for NRVE to be delivered
  //     to the address specified by their payment. Let's render the monitor, and give them instructions on how
  //     to pay for their niche.
  if (nrvePayment) {
    return <InvoiceNrvePaymentProcessing invoice={invoice} handleInvoiceUpdate={handleInvoiceUpdate} />;
  }

  // jw: if we do not have a payment form, or we are being requested to render the form, then let's do that.
  if (!fiatPayment || props.state.forcePayWithNrve) {
    return (
      <InvoicePayWithNrve
        invoice={invoice}
        handleForcePayWithNrve={handleForcePayWithNrve}
        handleInvoiceUpdate={handleInvoiceUpdate}
      />
    );
  }

  // jw: if we do not have a payment, but we have a fiat payment, then we need to render the ability to choose
  //     which payment option you want to use.
  return (
    <InvoicePayWithNrveOrCard
      {...props}
      invoice={invoice}
      handleForcePayWithNrve={handleForcePayWithNrve}
    />
  );
};

export const InvoicePaymentOptions = compose(
  withState({
    forcePayWithNrve: false
  }),
  withHandlers({
    handleForcePayWithNrve: (props: WithStateProps<State>) => (forcePayWithNrve: boolean) => {
      props.setState(ss => ({...ss, forcePayWithNrve}));
    }
  })
)(InvoicePaymentOptionsComponent) as React.ComponentClass<ParentProps>;
