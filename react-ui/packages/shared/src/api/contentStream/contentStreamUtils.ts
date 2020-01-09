import {
  ContentStreamEntries,
  ContentStreamScrollParams,
  ContentStreamSortOrder,
  Post,
  QualityFilter
} from '../../types';
import { GraphqlQueryControls } from 'react-apollo';
import { createLoadMorePropsFromQueryResults, CreateLoadMorePropsFromQueryResultsOptions } from '../utils';
import { stripUndefinedProperties } from '../../utils';

export const defaultContentStreamPostsPerPage = 25;

// jw: as much as I want to pick these out, we are forced to define this because we do not want the null defaults that
//     the GraphQL types define for optional fields... Really wish they used undefined.
export interface LoadMorePostsParams {
  lastItemOid?: string;
  lastItemDatetime?: string;
}

export interface ContentStreamFilters extends LoadMorePostsParams {
  qualityFilter?: QualityFilter;
  sortOrder?: ContentStreamSortOrder;
  forWidget?: boolean;
  count?: number;
}

export interface WithContentStreamProps {
  contentStreamLoading: boolean;
  posts: Post[];
  loadMorePosts?: () => Promise<void>;
  loadMorePostsParams?: LoadMorePostsParams;
}

// jw: due to how properties in TypeScript work, while the compiler limits us to what we have defined in our functional
//     scope, whatever was on the parent is going to be present on the props. So just because we do not see other
//     properties does not mean they aren't there. If we expand the props onto "variables.filters" then we will get all
//     properties, including the hidden ones, included in the rest request. So, let's extract just the properties we
//     need, and leave the rest behind.
export function extractContentStreamFilters(props: ContentStreamFilters) {
  const { qualityFilter, sortOrder, forWidget, count } = props;

  // jw: for recent only we want to include lastItemOid and lastItemDatetime
  let extraFilters;
  if (sortOrder === ContentStreamSortOrder.MOST_RECENT) {
    const { lastItemOid, lastItemDatetime } = props;

    extraFilters = { lastItemOid, lastItemDatetime };
  } else {
    extraFilters = {};
  }

  // jw: let's ensure we specify a default count here
  return stripUndefinedProperties({
    ...extraFilters,
    qualityFilter,
    sortOrder,
    forWidget,
    count: count || defaultContentStreamPostsPerPage
  });
}

/**
 * jw: utility functions necessary to parse featured infinite scroll results.
 */

function featuredScrollParamExtractor
  (data: ContentStreamEntries, itemsPerPage: number): ContentStreamScrollParams | null
{
  if (!data || !data.scrollParams || !data.scrollParams.nextItemOids) {
    return null;
  }

  let nextItemOids = data.scrollParams.nextItemOids;

  if (!nextItemOids.length) {
    return null;
  }

  nextItemOids = nextItemOids.slice(0, Math.min(nextItemOids.length, itemsPerPage));

  // @ts-ignore jw: we are not including the __typename property here, which is fine because this is used for the URL
  // parameters and we don't want it to be included.
  return {  nextItemOids };
}

// jw: the post oids that we still need to fetch will be persisted in the scrollParams.nextItemOids, so we need to move
//     that from the previous results into the next results, while removing the previously requested oids
function moveNextItemOidsBetweenResults(prevData: ContentStreamEntries, scrollParams: ContentStreamScrollParams) {
  if (!prevData || !prevData.scrollParams ) {
    return {};
  }

  let { nextItemOids } = prevData.scrollParams;
  if (!nextItemOids || !nextItemOids.length) {
    return {};
  }

  const requestedOids = scrollParams && scrollParams.nextItemOids || [];

  // jw: only keep the oids that have not been requested, which is inherently everything after these oids.
  // note: you can see where we sliced the requestedOids off the nextOids above in featuredScrollParamExtractor.
  nextItemOids = nextItemOids.slice(requestedOids.length);

  // jw: let's just replace the nextItemOids with the new one
  return { scrollParams: {
    // jw: to ensure that we maintain all of the fields defined in GraphQL, including __typename, we need to spread
      //   the original scrollParams to get all those base fields.
    ...prevData.scrollParams,
    nextItemOids
  }};
}

function hasMoreFeaturedItemsChecker(data: ContentStreamEntries): boolean {
  const nextItemOids = data && data.scrollParams && data.scrollParams.nextItemOids || [];

  return nextItemOids.length > 0;
}

/**
 * HOC to parse infinite scroll content stream results from query data
 */
export function createContentStreamPropsFromQueryResults
  <DataKey extends keyof QueryResult, QueryResult extends GraphqlQueryControls>
  (dataKey: DataKey, queryResults: QueryResult): WithContentStreamProps
{

  // jw: options we will be passing to the utility function will be different based on the type of query being ran.
  let options: CreateLoadMorePropsFromQueryResultsOptions<
      Post,
      ContentStreamScrollParams | null,
      ContentStreamEntries
    >
    | undefined;

  // jw: don't treat widget runs as featured, since we will never be loading more results for them.
  const forWidget = queryResults.variables.filters.forWidget;
  const sortOrder = queryResults.variables.filters.sortOrder;
  if (sortOrder === ContentStreamSortOrder.FEATURED && !forWidget) {
    const count = queryResults.variables.filters.count;

    options = {
      // jw: the scroll params are going to be a subset of the scrollParams.nextItemOids from the data.

      scrollParamExtractor: (data: ContentStreamEntries) => featuredScrollParamExtractor(data, count),
      // jw: note: _nextData is only defined because it is needed for the contract of this function
      getOriginalDataToPersist: moveNextItemOidsBetweenResults,
      // jw: finally, as long as we have nextItemOids on the data then we should continue to load more.
      hasMoreItemsChecker: hasMoreFeaturedItemsChecker
    };

  // jw: for MOST_RECENT we want to extract the lastItemOid and lastItemDatetime from the incoming data
  } else if (sortOrder === ContentStreamSortOrder.MOST_RECENT) {
    options = {
      extraDataExtractor: (data: ContentStreamEntries) => {
        if (!data || !data.scrollParams) {
          return {};
        }

        const { scrollParams: { lastItemOid, lastItemDatetime } } = data;

        return { loadMorePostsParams: { lastItemOid, lastItemDatetime } };
      }
    };
  }

  const loadMoreData = createLoadMorePropsFromQueryResults(dataKey, queryResults, options);

  const { items, loadMoreItems, loadMoreItemsLoading } = loadMoreData;

  // jw: because the type that comes back from `createLoadMorePropsFromQueryResults` does not define our
  //     `loadMorePostsParams` property we need to use reflection to look for it.
  let loadMorePostsParams;
  if ('loadMorePostsParams' in loadMoreData) {
    // tslint:disable-next-line no-string-literal
    loadMorePostsParams = loadMoreData['loadMorePostsParams'];
  }

  return {
    loadMorePostsParams,
    contentStreamLoading: loadMoreItemsLoading,
    posts: items,
    loadMorePosts: loadMoreItems
  };
}
