import * as React from 'react';
import { Tag } from '../../../../shared/components/Tag';
import { InvoiceStatus } from '@narrative/shared';
import { InvoiceMessages } from '../../../../shared/i18n/InvoiceMessages';
import { FormattedMessage } from 'react-intl';
import styled from '../../../../shared/styled';
import { InvoiceProps } from './AuctionInvoice';

const StatusTag = styled(Tag)`
  margin-left: 20px;
  
  &.ant-tag {
    text-transform: uppercase;
    max-width: 200px;
    text-align: center;
  }
`;

export const InvoiceStatusTag: React.SFC<InvoiceProps> = (props) => {
  const { invoice } = props;

  return (
    <StatusTag size="normal" notLinked={true}>
      {(invoice.status === InvoiceStatus.INVOICED && invoice.nrvePayment)
        ? <FormattedMessage {...InvoiceMessages.PaymentInProgress}/>
        : <FormattedMessage {...InvoiceMessages[`invoiceStatusTag.status.${invoice.status}`]}/>}
    </StatusTag>
  );
};
