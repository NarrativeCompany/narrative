import { graphql } from 'react-apollo';
import { FetchResult } from 'apollo-link';
import {
  NrvePaymentInput,
  PutInvoiceNrvePaymentMutation,
  PutInvoiceNrvePaymentMutation_putInvoiceNrvePayment
} from '../../types';
import { putInvoiceNrvePaymentMutation } from '../graphql/invoice/putInvoiceNrvePaymentMutation';
import { resolveExceptionFromApolloError } from '../../utils';

export interface WithPutInvoiceNrvePaymentProps {
  putInvoiceNrvePayment: (input: NrvePaymentInput, invoiceOid: string)
    => Promise<PutInvoiceNrvePaymentMutation_putInvoiceNrvePayment>;
}

export const withPutInvoiceNrvePayment = graphql(putInvoiceNrvePaymentMutation, {
  props: ({mutate}) => ({
    putInvoiceNrvePayment: async (input: NrvePaymentInput, invoiceOid: string) => {
      const variables = { input, invoiceOid };
      const options = {
        variables,
        mutation: putInvoiceNrvePaymentMutation
      };

      if (!mutate) {
        throw new Error ('withPutInvoiceNrvePayment: missing mutate');
      }

      return mutate(options)
        .then((response: FetchResult<PutInvoiceNrvePaymentMutation>) => {
          const putInvoiceNrvePayment =
            response &&
            response.data &&
            response.data.putInvoiceNrvePayment;

          if (!putInvoiceNrvePayment) {
            return;
          }

          return putInvoiceNrvePayment;
        }).catch((error) => {
          throw resolveExceptionFromApolloError(error);
        });
    }
  })
});
