import * as React from 'react';
import { Card } from '../../../../shared/components/Card';
import styled from '../../../../shared/styled';
import { Heading } from '../../../../shared/components/Heading';
import { Tooltip } from 'antd';
import { Paragraph } from '../../../../shared/components/Paragraph';

interface Props {
  heading: React.ReactNode;
  amountDue: React.ReactNode;
  subtitle: React.ReactNode;
  subtitleTooltip?: React.ReactNode;
}

const PaymentOptionCard = styled(Card)`
  text-align: center;
`;

const CardHeading = styled(Heading)`
  font-weight: 300;
  margin-bottom: 10px;
`;

export const InvoicePaymentOptionCard: React.SFC<Props> = (props) => {
  const { heading, amountDue, subtitle, subtitleTooltip } = props;
  const hasTooltip = subtitleTooltip != null;

  return (
    <PaymentOptionCard>
      <CardHeading size={2} textAlign="center">
        {heading}
      </CardHeading>
      <Heading size={3} textAlign="center">
        {amountDue}
      </Heading>
      <Paragraph textAlign="center">
        {hasTooltip ? <Tooltip title={subtitleTooltip}>{subtitle}</Tooltip>
          : subtitle}
      </Paragraph>
      {props.children}
    </PaymentOptionCard>
  );
};
