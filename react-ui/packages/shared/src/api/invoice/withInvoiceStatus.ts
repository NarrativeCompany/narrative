import { graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { invoiceStatusQuery } from '../graphql/invoice/invoiceStatusQuery';
import { InvoiceStatusQuery, InvoiceStatusQueryVariables } from '../../types';

interface ParentProps {
  invoiceOid: string;
  invoiceStatusPollInterval: number | null;
}

export type WithInvoiceStatusProps =
  NamedProps<{invoiceStatusData: GraphqlQueryControls & InvoiceStatusQuery}, ParentProps>;

export const withInvoiceStatus =
  graphql<
    ParentProps,
    InvoiceStatusQuery,
    InvoiceStatusQueryVariables
  >(invoiceStatusQuery, {
    skip: ({invoiceOid}) => !invoiceOid,
    options: ({invoiceOid, invoiceStatusPollInterval}) => ({
      variables: {invoiceOid},
      pollInterval: (invoiceStatusPollInterval) ? invoiceStatusPollInterval : 0
    }),
    name: 'invoiceStatusData'
  });
