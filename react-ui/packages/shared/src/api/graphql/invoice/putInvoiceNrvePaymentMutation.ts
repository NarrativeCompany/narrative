import gql from 'graphql-tag';
import { NrvePaymentFragment } from '../fragments/nrvePaymentFragment';

export const putInvoiceNrvePaymentMutation = gql`
  mutation PutInvoiceNrvePaymentMutation ($input: NrvePaymentInput!, $invoiceOid: String!) {
    putInvoiceNrvePayment (input: $input, invoiceOid: $invoiceOid) @rest(
      type: "NrvePayment"
      path: "/invoices/{args.invoiceOid}/nrve-payment", 
      method: "PUT"
    ) {
      ...NrvePayment
    }
  }
  ${NrvePaymentFragment}
`;
