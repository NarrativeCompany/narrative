import * as React from 'react';
import { RouteComponentProps } from 'react-router-dom';
import { NotFound } from '../../../../shared/components/NotFound';
import { InvoiceDetail } from '@narrative/shared';
import { AuctionInvoiceBody } from './AuctionInvoiceBody';

// jw: this interface is used throughout the Components for rendering the invoice. Let's centralize it to keep
//     it all consistent
export interface InvoiceProps {
  invoice: InvoiceDetail;
}

// jw: this is used by the RouteComponentProps and the AuctionInvoiceComponent to marshal the invoiceOid from
//     the route parameter, and through to apollo-rest.
export interface InvoiceOidProps {
  invoiceOid: string;
}

const AuctionInvoice: React.SFC<RouteComponentProps<InvoiceOidProps>> = (props) => {
  const invoiceOid = props.match.params.invoiceOid;

  if (!invoiceOid) {
    return <NotFound />;
  }

  return (
    <AuctionInvoiceBody invoiceOid={invoiceOid} />
  );
};

export default AuctionInvoice;
