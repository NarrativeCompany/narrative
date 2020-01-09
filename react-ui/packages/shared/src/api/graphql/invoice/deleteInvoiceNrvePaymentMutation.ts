import gql from 'graphql-tag';
import { InvoiceDetailFragment } from '../fragments/invoiceDetailFragment';

export const deleteInvoiceNrvePaymentMutation = gql`
  mutation DeleteInvoiceNrvePaymentMutation ($invoiceOid: String!) {
    deleteInvoiceNrvePayment (invoiceOid: $invoiceOid) @rest(
      type: "InvoiceDetail"
      path: "/invoices/{args.invoiceOid}/nrve-payment", 
      method: "DELETE"
    ) {
      ...InvoiceDetail
    }
  }
  ${InvoiceDetailFragment}
`;
