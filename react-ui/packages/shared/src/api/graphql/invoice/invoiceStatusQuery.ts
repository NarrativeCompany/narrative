import gql from 'graphql-tag';
import { InvoiceStatusDetailFragment } from '../fragments/invoiceStatusDetailFragment';

export const invoiceStatusQuery = gql`
  query InvoiceStatusQuery ($invoiceOid: String!) {
    getInvoiceStatus (invoiceOid: $invoiceOid) @rest(
      type: "InvoiceStatusDetail",
      path: "/invoices/{args.invoiceOid}/status",
      method: "GET"
    ) {
      ...InvoiceStatusDetail
    }
  }
  ${InvoiceStatusDetailFragment}
`;
