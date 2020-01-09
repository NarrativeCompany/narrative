// jw: extract posts from the entries results from the server.
import { stripUndefinedProperties, TYPENAME_FIELD_NAME } from '../../utils';
import { GraphqlQueryControls } from 'react-apollo';

export interface WithLoadMoreItemsProps<T> {
  loadMoreItemsLoading: boolean;
  items: T[];
  loadMoreItems?: () => Promise<void>;
}

export interface LoadMoreItems<T> {
  items: T[];
  hasMoreItems: boolean;
}

export interface ScrollableLoadMoreItems<T, SP> extends LoadMoreItems<T> {
  scrollParams: SP;
}

function extractItems<T, LMI extends LoadMoreItems<T>>(data: LMI): T[] {
  return data && data.items || [];
}

// jw: derive filters from a set of results from the server.
function extractScrollParams<T, SP, D extends ScrollableLoadMoreItems<T, SP>>(
  data: D,
  extractor?: (data: D) => SP
): SP | {} {
  if (!data || !data.scrollParams) {
    return {};
  }

  let params;
  if (extractor) {
    params = extractor(data);

  // jw: this is worth explaining: To prevent all callers from having to extract their scrollParams, let's assume that
  //   everything in the datas.scrollProps is valid except for the __typename property. We never want to push that one
  //   forward.
  } else {
    params = data.scrollParams;
    // tslint:disable-next-line: no-string-literal
    if (params[TYPENAME_FIELD_NAME]) {
      // jw: first, clone the object because we do not want to modify the original
      params = Object.assign({}, params);
      delete params[TYPENAME_FIELD_NAME];
    }
  }

  // jw: we want to make sure we only maintain the filters that actually exist (are not null or undefined)
  return stripUndefinedProperties(params);
}

// jw: the options for the utility method are getting rather large, so let's try and group that all together.
export interface CreateLoadMorePropsFromQueryResultsOptions<T, SP,  D extends ScrollableLoadMoreItems<T, SP>> {
  // jw: need to figure out how  to define SP so that it naturally includes __typename, and then we can strip it out
  //     of these definitions. It kinda sucks that the type always ends up having it, but we cannot remove it here.
  scrollParamExtractor?: (data: D) => SP;

  // jw: for some results the ability to load more is not dictated by the data.hasMoreItems flag, so let's allow the
  //     caller to customize this behavior if necessary.
  hasMoreItemsChecker?: (data: D) => boolean;

  // jw: in some cases we will want to pestist values from the original object down to the new object when getting
  //     new results. Thus, let's give the caller a chance to do that.
  getOriginalDataToPersist?: (prevData: D, nextParams: SP | {}) => {};

  // jw: this function gives the caller a chance to extract more information from the server results and include it
  //     in the resulting properties.
  extraDataExtractor?: (data: D) => {};
}

// jw: This utility function will parse the results from a Query, and massage them into the WithContentStreamProps
export function createLoadMorePropsFromQueryResults<
    DataKey extends keyof QueryResult,
    QueryResult extends GraphqlQueryControls,
    T,
    SP,
    D extends ScrollableLoadMoreItems<T, SP>,
    R extends WithLoadMoreItemsProps<T>
  >
  (
    dataKey: DataKey,
    queryResults: QueryResult,
    options?: CreateLoadMorePropsFromQueryResultsOptions<T, SP, D>
  ): R
{
  const { loading, fetchMore, variables } = queryResults;

  // @ts-ignore
  const data = queryResults[dataKey] as D;

  // jw: extract out any items that may already be cached into the query. The new results will be added shortly.
  const items = extractItems<T, D>(data);

  // jw: if we have an extraDataExtractor, let's use it to extract more data properties.
  const extraData = (options && options.extraDataExtractor)
    ? options.extraDataExtractor(data)
    : {};

  // jw: need to add the extra grouping to ensure that the 'undefined' is not considered an option for the
  //     function result, but rather an alternative variable value.
  let loadMoreItems: (() => Promise<void>) | undefined;

  let hasMoreItems = false;
  if (data) {
    hasMoreItems = (options && options.hasMoreItemsChecker)
        ? options.hasMoreItemsChecker(data)
        : data.hasMoreItems;
  }

  // jw: if we have more items, then we need to construct the loadMoreItems defined above.
  if (hasMoreItems) {
    loadMoreItems = async () => {
      // jw: now that we are being invoked, let's extract the next set of filters so that we can use the previous
      //     variables to request the next set of results.
      const nextScrollParams = extractScrollParams(data, options && options.scrollParamExtractor);

      // jw: this does the work, fetch more results from the server
      await fetchMore({
        variables: {
          // jw: spread the original variables out before the new ones.
          ...variables,
          filters: {
            // jw: spread the original filters out before the new ones.
            ...variables.filters,
            // jw: spread the new filters out, so they override the originals.
            // jw: because nextScrollParams is typed from a generic it is not recognized as an object... Force it)
            ...(nextScrollParams as object)
          }
        },
        // jw: prevResults represents our original results, while fetchMoreResult are the next set of results provided
        //     by fetchMore above.
        updateQuery: (prevResults, { fetchMoreResult }) => {
          const prevData: D = prevResults[dataKey];

          // jw: if we don't have any new results return the previous ones but turn hasMoreItems off, that way we do
          //     not include the loadMoreItems function again.
          if (!fetchMoreResult) {
            return {
              [dataKey]: {
                // jw: because prevData is typed from a generic it is not recognized as an object... Force it)
                ...(prevData as object),
                hasMoreItems: false
              }
            };
          }

          const nextData: D = fetchMoreResult[dataKey];

          // jw: extract the items from both sets of results
          const prevItems = extractItems<T, D>(prevData);
          const newItems = extractItems<T, D>(nextData);

          // jw: merge the fresh result to the previous and write the new values to the store
          return {
            [dataKey]: {
              // jw: spread all of the fields from the new object first.
              // jw: because newData is typed from a generic it is not recognized as an object... Force it)
              ...(nextData as object),
              // jw: give the caller a chance to persist some other properties forward from the original object
              ...((options && options.getOriginalDataToPersist)
                ? options.getOriginalDataToPersist(prevData, nextScrollParams)
                : {}
              ),
              // jw: then override the 'items' with a merged set of items.
              items: [
                ...prevItems,
                ...newItems
              ]
            }
          };
        }
      });
    };
  }

  // @ts-ignore jw: even though this lines up with R perfectly, tslint doesn't see it.
  return { loadMoreItemsLoading: loading, items, loadMoreItems, ...extraData };
}
