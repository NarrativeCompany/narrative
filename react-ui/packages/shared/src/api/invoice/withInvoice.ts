import { graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { invoiceQuery } from '../graphql/invoice/invoiceQuery';
import { InvoiceQuery, InvoiceQueryVariables } from '../../types';

interface ParentProps {
  invoiceOid: string;
}

export type WithInvoiceProps =
  NamedProps<{invoiceData: GraphqlQueryControls & InvoiceQuery}, ParentProps>;

export const withInvoice =
  graphql<
    ParentProps,
    InvoiceQuery,
    InvoiceQueryVariables
  >(invoiceQuery, {
    skip: ({invoiceOid}) => !invoiceOid,
    options: ({invoiceOid}) => ({
      variables: {invoiceOid}
    }),
    name: 'invoiceData'
  });
