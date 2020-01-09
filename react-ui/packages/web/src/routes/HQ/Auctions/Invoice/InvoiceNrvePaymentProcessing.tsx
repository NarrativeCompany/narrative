import * as React from 'react';
import { InvoiceProps } from './AuctionInvoice';
import { InvoiceCancelNrvePaymentButton } from './InvoiceCancelNrvePaymentButton';
import { Card } from '../../../../shared/components/Card';
import { Spin, Tooltip } from 'antd';
import { FormattedMessage } from 'react-intl';
import { InvoiceMessages } from '../../../../shared/i18n/InvoiceMessages';
import { Paragraph } from '../../../../shared/components/Paragraph';
import { Heading } from '../../../../shared/components/Heading';
import { FlexContainer } from '../../../../shared/styled/shared/containers';
import styled from '../../../../shared/styled';
import { HandleInvoiceUpdateCallback } from './InvoiceDetails';
import { Link } from '../../../../shared/components/Link';

const SpinnerContainer = styled(FlexContainer)`
    margin: 10px 0;
  }
`;

const PaymentProcessingCard = styled(Card)`
  &.ant-card {
    margin-bottom: 15px;
  }
`;

type Props = InvoiceProps &
  HandleInvoiceUpdateCallback;

export const InvoiceNrvePaymentProcessing: React.SFC<Props> = (props) => {
  const { invoice, handleInvoiceUpdate } = props;
  const { nrvePayment } = invoice;

  const nrveValue = invoice.nrveAmount;
  const paymentNeoAddress = nrvePayment && nrvePayment.paymentNeoAddress;
  const yourNeoAddress = !nrvePayment ?
    <FormattedMessage {...InvoiceMessages.YourNeoAddress}/> : (
    <Tooltip title={nrvePayment.fromNeoAddress}>
      <span><FormattedMessage {...InvoiceMessages.YourNeoAddress}/></span>
    </Tooltip>
  );

  const nrveLink = <Link.About type="nrve"/>;

  return (
    <React.Fragment>
      <PaymentProcessingCard>
        <Paragraph>
          <FormattedMessage {...InvoiceMessages.PaymentInstructions} values={{nrveValue, nrveLink, yourNeoAddress}}/>
        </Paragraph>
        <Heading size={3} textAlign="center">
          {paymentNeoAddress}
        </Heading>
      </PaymentProcessingCard>
      <PaymentProcessingCard>
        <Heading size={2} textAlign="center">
          <FormattedMessage {...InvoiceMessages.MonitoringPaymentProgress} />
        </Heading>
        <SpinnerContainer centerAll={true}>
          <Spin size="large" />
        </SpinnerContainer>
        <Paragraph size="small" textAlign="center">
          <FormattedMessage {...InvoiceMessages.PageWillReload}/>
        </Paragraph>
        <InvoiceCancelNrvePaymentButton invoice={invoice} handleInvoiceUpdate={handleInvoiceUpdate} />
      </PaymentProcessingCard>
    </React.Fragment>
  );
};
