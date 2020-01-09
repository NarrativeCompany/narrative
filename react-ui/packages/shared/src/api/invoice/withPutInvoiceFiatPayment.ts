import { graphql } from 'react-apollo';
import { FetchResult } from 'apollo-link';
import {
  FiatPaymentInput,
  PutInvoiceFiatPaymentMutation,
  PutInvoiceFiatPaymentMutation_putInvoiceFiatPayment
} from '../../types';
import { putInvoiceFiatPaymentMutation } from '../graphql/invoice/putInvoiceFiatPaymentMutation';
import { resolveExceptionFromApolloError } from '../../utils';

export interface WithPutInvoiceFiatPaymentProps {
  putInvoiceFiatPayment: (input: FiatPaymentInput, invoiceOid: string)
    => Promise<PutInvoiceFiatPaymentMutation_putInvoiceFiatPayment>;
}

export const withPutInvoiceFiatPayment = graphql(putInvoiceFiatPaymentMutation, {
  props: ({mutate}) => ({
    putInvoiceFiatPayment: async (input: FiatPaymentInput, invoiceOid: string) => {
      const variables = { input, invoiceOid };
      const options = {
        variables,
        mutation: putInvoiceFiatPaymentMutation
      };

      if (!mutate) {
        throw new Error ('withPutInvoiceFiatPayment: missing mutate');
      }

      return mutate(options)
        .then((response: FetchResult<PutInvoiceFiatPaymentMutation>) => {
          const putInvoiceFiatPayment =
            response &&
            response.data &&
            response.data.putInvoiceFiatPayment;

          if (!putInvoiceFiatPayment) {
            throw new Error('withPutInvoiceFiatPayment: no return value from putInvoiceFiatPayment mutation');
          }

          return putInvoiceFiatPayment;
        }).catch((error) => {
          throw resolveExceptionFromApolloError(error);
        });
    }
  })
});
