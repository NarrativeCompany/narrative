import gql from 'graphql-tag';
import { NrveValueDetailImplFragment } from './nrveValueDetailImplFragment';
import { InvoiceDetailFragment } from './invoiceDetailFragment';

export const PublicationInvoiceFragment = gql`
  fragment PublicationInvoice on PublicationInvoice {
    oid

    plan
    newEndDatetime
    estimatedRefundAmount @type(name: "NrveValueDetailImpl") {
       ...NrveValueDetailImpl
     }
    invoiceDetail @type(name: "InvoiceDetail") {
      ...InvoiceDetail
    }
  }
  ${NrveValueDetailImplFragment}
  ${InvoiceDetailFragment}
`;
