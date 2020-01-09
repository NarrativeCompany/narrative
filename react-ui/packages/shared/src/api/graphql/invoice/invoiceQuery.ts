import gql from 'graphql-tag';
import { InvoiceDetailFragment } from '../fragments/invoiceDetailFragment';

export const invoiceQuery = gql`
  query InvoiceQuery ($invoiceOid: String!) {
    getInvoice (invoiceOid: $invoiceOid) @rest(
      type: "InvoiceDetail", 
      path: "/invoices/{args.invoiceOid}",
      method: "GET"
    ) {
      ...InvoiceDetail
    }
  }
  ${InvoiceDetailFragment}
`;
