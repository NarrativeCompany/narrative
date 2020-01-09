import gql from 'graphql-tag';

export const LedgerEntryScrollParamsFragment = gql`
  fragment LedgerEntryScrollParams on LedgerEntryScrollParams {
    lastItemDatetime
  }
`;
