import gql from 'graphql-tag';
import { PublicationInvoiceFragment } from '../fragments/publicationInvoiceFragment';

export const createPublicationInvoiceMutation = gql`
  mutation CreatePublicationInvoiceMutation ($input: PublicationPlanInput!, $publicationOid: String!) {
    createPublicationInvoice (input: $input, publicationOid: $publicationOid) @rest(
      type: "InvoiceDetail", 
      path: "/publications/{args.publicationOid}/plan", 
      method: "PUT"
    ) {
      ...PublicationInvoice
    }
  }
  ${PublicationInvoiceFragment}
`;
