import * as React from 'react';
import { InvoiceProps } from './AuctionInvoice';
import { Col, Row } from 'antd';
import { InvoicePaymentOptionCard } from './InvoicePaymentOptionCard';
import { InvoiceMessages } from '../../../../shared/i18n/InvoiceMessages';
import { FormattedMessage } from 'react-intl';
import { CardPaymentDataProps, HandleForcePayWithNrveCallback } from './InvoicePaymentOptions';
import { Heading } from '../../../../shared/components/Heading';
import styled from '../../../../shared/styled';
import { InvoicePayWithCardButton } from './InvoicePayWithCardButton';
import { HandleInvoiceUpdateCallback } from './InvoiceDetails';
import { Button } from '../../../../shared/components/Button';
import { USD } from '../../../../shared/components/USD';
import { NRVE } from '../../../../shared/components/NRVE';
import { Link } from '../../../../shared/components/Link';

type Props = InvoiceProps &
  HandleForcePayWithNrveCallback &
  HandleInvoiceUpdateCallback &
  CardPaymentDataProps;

const PaymentOptionsHeading = styled(Heading)`
  margin-bottom: 5px;
`;

export const InvoicePayWithNrveOrCard: React.SFC<Props> = (props) => {
  const { invoice, handleForcePayWithNrve, handleInvoiceUpdate, cardCustomPaymentData, cardPaymentDescription } = props;
  const { fiatPayment } = invoice;

  if (!fiatPayment) {
    // todo: we should record an error with the server, since we should never have been called in this state
    return null;
  }

  const nrveLink = <Link.About type="nrve" size="inherit"/>;

  return (
    <React.Fragment>
      <PaymentOptionsHeading size={3}>
        <FormattedMessage {...InvoiceMessages.HowWouldYouLikeToPay}/>
      </PaymentOptionsHeading>
      <Row gutter={16}>
        <Col md={12}>
          <InvoicePaymentOptionCard
            heading={<FormattedMessage {...InvoiceMessages.PayWithNrve} values={{ nrveLink }}/>}
            amountDue={<NRVE amount={invoice.nrveAmount} />}
            subtitle={<FormattedMessage {...InvoiceMessages.NoFees} />}
          >
            <Button onClick={() => {
              if (handleForcePayWithNrve) {
                handleForcePayWithNrve(true);
              }
            }}>
              <FormattedMessage {...InvoiceMessages.Select}/>
            </Button>

          </InvoicePaymentOptionCard>
        </Col>
        <Col md={12}>
          <InvoicePaymentOptionCard
            heading={<FormattedMessage {...InvoiceMessages.PayWithPayPal} />}
            amountDue={<FormattedMessage
              {...InvoiceMessages.PaymentValueByCard}
              values={{fiatPayment: <USD value={fiatPayment.totalUsdAmount} />}}
            />}
            subtitle={<FormattedMessage
              {...InvoiceMessages.IncludesConvenienceFee}
              values={{convenienceFee: <USD value={fiatPayment.feeUsdAmount} />}}
            />}
            subtitleTooltip={<FormattedMessage {...InvoiceMessages.IncludesConvenienceFeeTooltip} />}
          >
            <InvoicePayWithCardButton
              invoice={invoice}
              handleInvoiceUpdate={handleInvoiceUpdate}
              customPaymentData={cardCustomPaymentData}
              paymentDescription={cardPaymentDescription}
            />
          </InvoicePaymentOptionCard>
        </Col>
      </Row>
    </React.Fragment>
  );
};
