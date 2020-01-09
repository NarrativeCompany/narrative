import gql from 'graphql-tag';

export const InvoiceFragment = gql`
  fragment Invoice on Invoice {
    oid
    type
  }
`;
