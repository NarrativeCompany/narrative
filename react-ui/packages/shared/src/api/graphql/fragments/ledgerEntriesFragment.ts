import gql from 'graphql-tag';
import { LedgerEntryFragment } from '../fragments/ledgerEntryFragment';
import { LedgerEntryScrollParamsFragment } from '../fragments/ledgerEntryScrollParamsFragment';

export const LedgerEntriesFragment = gql`
  fragment LedgerEntries on LedgerEntries {
    items @type(name: "LedgerEntry") {
      ...LedgerEntry
    }
    hasMoreItems
    scrollParams @type(name: "LedgerEntryScrollParams") {
      ...LedgerEntryScrollParams
    }
  }
  ${LedgerEntryFragment}
  ${LedgerEntryScrollParamsFragment}
`;
