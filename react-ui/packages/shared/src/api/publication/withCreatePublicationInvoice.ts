import { graphql } from 'react-apollo';
import { createPublicationInvoiceMutation } from '../graphql/publication/createPublicationInvoiceMutation';
import {
  PublicationInvoice,
  PublicationPlanInput,
  CreatePublicationInvoiceMutation,
  CreatePublicationInvoiceMutationVariables,
} from '../../types';
import { mutationResolver } from '../../utils';

const functionName = 'createPublicationInvoice';

export interface WithCreatePublicationInvoiceProps {
  [functionName]: (input: PublicationPlanInput, publicationOid: string) => Promise<PublicationInvoice>;
}

export const withCreatePublicationInvoice =
  graphql<
    {},
    CreatePublicationInvoiceMutation,
    CreatePublicationInvoiceMutationVariables,
    WithCreatePublicationInvoiceProps
  >(createPublicationInvoiceMutation, {
    props: ({mutate}) => ({
      [functionName]: async (input: PublicationPlanInput, publicationOid: string) => {
        return await mutationResolver<CreatePublicationInvoiceMutation>(mutate, {
          variables: { input, publicationOid }
        }, functionName);
      }
    })
  });
