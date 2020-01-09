import * as React from 'react';
import { compose, lifecycle, withProps } from 'recompose';
import {
  InvoiceStatus,
  InvoiceStatusDetail,
  withInvoiceStatus,
  WithInvoiceStatusProps
} from '@narrative/shared';
import { InvoiceProps } from './AuctionInvoice';
import { HandleInvoiceUpdateCallback } from './InvoiceDetails';

type ParentProps = InvoiceProps &
  HandleInvoiceUpdateCallback;

type Props = WithInvoiceStatusProps &
  HandleInvoiceUpdateCallback &
  {
    paymentStatus: InvoiceStatusDetail;
    loading: boolean;
  };

// jw: ideally we would just get rid of this, since the HOC processing handles everything.
const InvoiceNrvePaymentProcessPollComponent: React.SFC<Props> = () => {
  return null;
};

// jw: let's create a utility function to process the properties, since we will want to process these when the
//     componentDidMount, and when the componentWillUpdate. This is because we will be causing InvoiceDetails
//     to update via a callback handler once we get a paid invoice, and doing that during the render of the
//     component causes an error:
// tslint:disable-next-line: max-line-length
// Cannot update during an existing state transitionz (such as within `render` or another component's constructor). Render methods should be a pure function of props and state; constructor side-effects are an anti-pattern, but can be moved to `componentWillMount`
function processStatusFromProps(props: Props): void {
  const { paymentStatus, loading, handleInvoiceUpdate, invoiceStatusData } = props;

  // jw: ignore all loading events.
  if (loading) {
    return;
  }

  // jw: just short out and wait for the next cycle if we are still invoiced.
  if (paymentStatus.status === InvoiceStatus.INVOICED) {
    return;
  }

  // jw: since the payment is complete, let's stop polling now.
  invoiceStatusData.stopPolling();

  // jw: if we have a invoice object, let's update the invoice on the parent
  if (paymentStatus.invoice) {
    handleInvoiceUpdate(paymentStatus.invoice);
  }
}

export const InvoiceNrvePaymentProcessPolling = compose(
  withProps((props: InvoiceProps) => {
    const { invoice: {oid} } = props;

    return {
      invoiceOid: oid,
      // jw: let's run this query every 5 seconds as needed
      invoiceStatusPollInterval: 5000
    };
  }),
  withInvoiceStatus,
  withProps((props: WithInvoiceStatusProps) => {
    const { invoiceStatusData: {getInvoiceStatus, loading} } = props;

    return { paymentStatus: getInvoiceStatus, loading};
  }),
  // jw: See comment above on the processStatusFromProps function for explanation of why we are using these
  //     lifecycle events for processing.
  lifecycle({
    // jw: passing the objects this way because the name: anonymous function method does not provide access to the
    //     this entity, which we need for componentDidMount
    componentDidMount() {
      processStatusFromProps(this.props as Props);
    },
    componentWillUpdate(props: Props) {
      processStatusFromProps(props);
    }
  })
)(InvoiceNrvePaymentProcessPollComponent) as React.ComponentClass<ParentProps>;
