import gql from 'graphql-tag';
import { InvoiceDetailFragment } from '../fragments/invoiceDetailFragment';

export const putInvoiceFiatPaymentMutation = gql`
  mutation PutInvoiceFiatPaymentMutation ($input: FiatPaymentInput!, $invoiceOid: String!) {
    putInvoiceFiatPayment (input: $input, invoiceOid: $invoiceOid) @rest(
      type: "InvoiceDetail"
      path: "/invoices/{args.invoiceOid}/fiat-payment", 
      method: "PUT"
    ) {
      ...InvoiceDetail
    }
  }
  ${InvoiceDetailFragment}
`;
