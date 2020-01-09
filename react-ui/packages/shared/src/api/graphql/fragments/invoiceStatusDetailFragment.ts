import gql from 'graphql-tag';

import { InvoiceDetailFragment } from './invoiceDetailFragment';

export const InvoiceStatusDetailFragment = gql`
  fragment InvoiceStatusDetail on InvoiceStatusDetail {
    status
    invoice @type(name: "InvoiceDetail") {
      ...InvoiceDetail
    }
  }
  ${InvoiceDetailFragment}
`;
