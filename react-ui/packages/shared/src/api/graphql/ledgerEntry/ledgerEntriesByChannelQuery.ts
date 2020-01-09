import gql from 'graphql-tag';
import { LedgerEntriesFragment } from '../fragments/ledgerEntriesFragment';

export const ledgerEntriesByChannelQuery = gql`
  query LedgerEntriesByChannelQuery ($filters: LedgerEntriesQueryInput, $channelOid: String!) {
    getLedgerEntriesByChannel (filters: $filters, channelOid: $channelOid) 
    @rest (
      type: "LedgerEntries", 
      path: "/ledger-entries/channel/{args.channelOid}?{args.filters}"
    ) {
      ...LedgerEntries
    }
  }
  ${LedgerEntriesFragment}
`;
