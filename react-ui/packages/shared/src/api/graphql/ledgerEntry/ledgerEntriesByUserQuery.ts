import gql from 'graphql-tag';
import { LedgerEntriesFragment } from '../fragments/ledgerEntriesFragment';

export const ledgerEntriesByUserQuery = gql`
  query LedgerEntriesByUserQuery ($filters: LedgerEntriesQueryInput, $userOid: String!) {
    getLedgerEntriesByUser (filters: $filters, userOid: $userOid) 
    @rest (
      type: "LedgerEntries", 
      path: "/ledger-entries/user/{args.userOid}?{args.filters}"
    ) {
      ...LedgerEntries
    }
  }
  ${LedgerEntriesFragment}
`;
