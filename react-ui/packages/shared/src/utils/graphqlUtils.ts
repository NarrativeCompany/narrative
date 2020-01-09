import { GraphqlQueryControls, QueryOpts } from 'react-apollo';
import { PageInfo } from '../types';

export const TYPENAME_FIELD_NAME = '__typename';

/**
 * Helper to test if a GraphQL object is "empty" - i.e. no fields set other than type.
 * This function is useful for testing if a GraphQL object is "present" in Apollo link state.  Link state does not
 * provide a way to remove or invalidate a cache entry so the only work around is to write an empty object.
 */
// tslint:disable-next-line no-any
export function isGraphQLObjectEmpty (object: any) {
  // GraphQL objects will always have a '__type' field value.  An empty object will only have this field value defined.
  if (object) {
    const populatedFieldCount = Object.keys(object)
      .filter(key => {
        return TYPENAME_FIELD_NAME !== key;
      })
      .reduce((sum, key) => {
        return sum + ((object[key]) ? 1 : 0);
      }, 0);
    return populatedFieldCount === 0;
  }

  return true;
}

export function getPageableQueryProps <QueryType, ItemsType>(
  data: GraphqlQueryControls & QueryType,
  functionName: string
): { loading: boolean; items: ItemsType[]; pageInfo: PageInfo} {
  const loading = data.loading;
  const result =
    data &&
    data[functionName];
  const items =
    result &&
    result.items || [];
  const pageInfo =
    result &&
    result.info;

  return { loading, items, pageInfo };
}

// jw: apollo-react seems to be failing to update the loading status on queries that have object arrays in them.
//     this setting, which defaults to false, solves that problem. See:
// https://github.com/apollographql/apollo-client/issues/1186#issuecomment-323513354
export const infiniteLoadingFixProps: QueryOpts = {
  notifyOnNetworkStatusChange: true
};

export interface LoadingProps {
  loading: boolean;
}
