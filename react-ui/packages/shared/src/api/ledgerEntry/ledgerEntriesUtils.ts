import { LedgerEntry } from '../../types';
import { GraphqlQueryControls } from 'react-apollo';
import { createLoadMorePropsFromQueryResults } from '../utils';

const resultsPerPage = 25;

export interface LedgerEntriesParentProps {
  count?: number;
}

export interface WithLedgerEntriesProps {
  ledgerEntriesLoading: boolean;
  entries: LedgerEntry[];
  loadMoreEntries?: () => Promise<void>;
}

// jw: due to how properties in TypeScript work, while the compiler limits us to what we have defined in our functional
//     scope, whatever was on the parent is going to be present on the props. So just because we do not see other
//     properties does not mean they aren't there. If we expand the props onto "variables.filters" then we will get all
//     properties, including the hidden ones, included in the rest request. So, let's extract just the properties we
//     need, and leave the rest behind.
export function extractLedgerEntriesFilters(props: LedgerEntriesParentProps) {
  const { count } = props;

  // jw: let's ensure we specify a default count here
  return { count: count || resultsPerPage };
}

// jw: This utility function will parse the results from a Query, and massage them into the WithContentStreamProps
export function createLedgerEntriesPropsFromQueryResults
  <DataKey extends keyof QueryResult, QueryResult extends GraphqlQueryControls>
  (dataKey: DataKey, queryResults: QueryResult): WithLedgerEntriesProps
{
  const loadMoreData = createLoadMorePropsFromQueryResults(dataKey, queryResults);

  const { items, loadMoreItems, loadMoreItemsLoading } = loadMoreData;

  return {
    ledgerEntriesLoading: loadMoreItemsLoading,
    entries: items as LedgerEntry[],
    loadMoreEntries: loadMoreItems
  };
}
