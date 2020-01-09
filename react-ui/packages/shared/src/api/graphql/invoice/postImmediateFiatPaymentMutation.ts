import gql from 'graphql-tag';
import { InvoiceDetailFragment } from '../fragments/invoiceDetailFragment';

export const postImmediateFiatPaymentMutation = gql`
  mutation PostImmediateFiatPaymentMutation ($input: ImmediateFiatPaymentInput!) {
    postImmediateFiatPayment (input: $input) @rest(
      type: "InvoiceDetail"
      path: "/invoices/fiat-payment", 
      method: "POST"
    ) {
      ...InvoiceDetail
    }
  }
  ${InvoiceDetailFragment}
`;
