import { graphql } from 'react-apollo';
import { FetchResult } from 'apollo-link';
import {
  DeleteInvoiceNrvePaymentMutation,
  DeleteInvoiceNrvePaymentMutation_deleteInvoiceNrvePayment
} from '../../types';
import { deleteInvoiceNrvePaymentMutation } from '../graphql/invoice/deleteInvoiceNrvePaymentMutation';
import { resolveExceptionFromApolloError } from '../../utils';

export interface WithDeleteInvoiceNrvePaymentProps {
  deleteInvoiceNrvePayment: (invoiceOid: string)
    => Promise<DeleteInvoiceNrvePaymentMutation_deleteInvoiceNrvePayment>;
}

export const withDeleteInvoiceNrvePayment = graphql(deleteInvoiceNrvePaymentMutation, {
  props: ({mutate}) => ({
    deleteInvoiceNrvePayment: async (invoiceOid: string) => {
      const variables = { invoiceOid };
      const options = {
        variables,
        mutation: deleteInvoiceNrvePaymentMutation
      };

      if (!mutate) {
        throw new Error ('withDeleteInvoiceNrvePayment: missing mutate');
      }

      return mutate(options)
        .then((response: FetchResult<DeleteInvoiceNrvePaymentMutation>) => {
          const deleteInvoiceNrvePayment =
            response &&
            response.data &&
            response.data.deleteInvoiceNrvePayment;

          if (!deleteInvoiceNrvePayment) {
            throw new Error('withDeleteInvoiceNrvePayment: no return value from deleteInvoiceNrvePayment mutation');
          }

          return deleteInvoiceNrvePayment;
        }).catch((error) => {
          throw resolveExceptionFromApolloError(error);
        });
    }
  })
});
