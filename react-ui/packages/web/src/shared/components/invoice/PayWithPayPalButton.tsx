import * as React from 'react';
import { includeScript, PayPalCheckoutDetails } from '@narrative/shared';
import { Modal } from 'antd';
import { FormattedMessage } from 'react-intl';
import { InvoiceMessages } from '../../i18n/InvoiceMessages';
import { compose } from 'recompose';
import { withExtractedCurrentUser, WithExtractedCurrentUserProps } from '../../containers/withExtractedCurrentUser';

interface ParentProps {
  checkoutDetails: PayPalCheckoutDetails;
  paymentDescription: string;
  // jw: this will appear in the custom field on the payment that comes back from PayPal
  customPaymentData?: string;
  handlePaymentId: (paymentId: string) => void;
  processingPayment: boolean;
}

type Props =
  ParentProps &
  WithExtractedCurrentUserProps;

class PayWithPayPalButtonComponent extends React.Component<Props> {
  constructor(props: Props) {
    super(props);
  }

  componentDidMount() {
    // jw: once we mount, let's load PayPal's lib and then setup the payment handler
    includeScript('https://www.paypalobjects.com/api/checkout.js', () => {
      const { checkoutDetails, currentUser } = this.props;

      if (!checkoutDetails) {
        // todo:error-handling: we should report this unexpected case!
        return;
      }

      if (!currentUser) {
        // todo:error-handling: we should report this unexpected case!
        return;
      }

      const isSandbox = checkoutDetails.clientMode === 'sandbox';
      const sandbox = isSandbox ? `${checkoutDetails.clientId}` : '';
      const production = isSandbox ? '' : `${checkoutDetails.clientId}`;
      const { amountForPayPal } = checkoutDetails;

      const description = this.props.paymentDescription;

      let custom = `user/${currentUser.oid} handle/${currentUser.username}`;
      if (this.props.customPaymentData) {
        custom = `${this.props.customPaymentData} ${custom}`;
      }
      const handlePaymentId = this.props.handlePaymentId;

      // jw: there is a delay between when the button renders and when it is clickable. This is due to how PayPal is
      //     pre-rendering a button as an indicator, but then not tracking clicks to that button, and just replacing it
      //     once their tools load they can use a live button. In 'production' mode this is supposed to be much faster,
      //     but a ultimate fix will be coming once the new major version is released. Supposed to be within a month:
      // https://github.com/paypal/paypal-checkout/issues/471

      // tslint:disable-next-line no-any
      (window as any).paypal.Button.render({
        // Configure environment
        env: `${checkoutDetails.clientMode}`,
        client: { sandbox, production },
        // Customize button (optional)
        locale: 'en_US',
        style: {
          size: 'medium',
          color: 'white',
          shape: 'pill',
          label: 'paypal',
          tagline: false,
        },

        // Enable Pay Now checkout flow (optional)
        commit: true,

        // Set up a payment
        // tslint:disable-next-line: no-any object-literal-shorthand
        payment: function(_data: any, actions: any) {
          return actions.payment.create({
            application_context: {
              shipping_preference: 'NO_SHIPPING'
            },
            transactions: [{
              amount: {
                total: `${amountForPayPal}`,
                currency: 'USD'
              },
              custom,
              description
            }]
          });
        },
        // Execute the payment
        // tslint:disable-next-line: no-any object-literal-shorthand
        onAuthorize: function(data: any, actions: any) {
          return actions.payment.execute().then(() => {
            // jw: the form will load the modal and close the modal once processing is complete.
            handlePaymentId(data.paymentID);
          });
        }
      }, '#paypal-button');
    });
  }

  render() {
    return (
      <React.Fragment>
        <Modal
          title={<FormattedMessage {...InvoiceMessages.ProcessingPaymentTitle} />}
          visible={this.props.processingPayment}
          footer={null}
          closable={false}
          maskClosable={false}
        >
          <FormattedMessage {...InvoiceMessages.ProcessingPaymentMessage} />
        </Modal>
        {/*
        jw: this container is where the PayPal button will be rendered.
        jw: note: if we ever want to support more than one button on a page, we will need to make this ID configurable.
        */}
        <div id="paypal-button" />
      </React.Fragment>
    );
  }
}

export const PayWithPayPalButton = compose(
  withExtractedCurrentUser
)(PayWithPayPalButtonComponent) as React.ComponentClass<ParentProps>;
