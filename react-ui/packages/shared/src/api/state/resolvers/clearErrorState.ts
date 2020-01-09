import { buildApolloCacheErrorState, buildEmptyErrorState } from './setErrorState';

/**
 * Clear the error state in the Apollo link cache.  This function is idempotent and therefore can be called multiple
 * times safely even if there is no error state in the cache.
 */
// tslint:disable-next-line no-any
export const clearErrorState = (_: any, _input: any, { cache }: any) => {
  // Write an "empty" object to the cache indicating the entry is in an uninitialized state.  Empty is as good as it
  // gets with GraphQL objects in the cache because there is no way to invalidate an entry in the cache :/
  const data = buildApolloCacheErrorState(buildEmptyErrorState());
  cache.writeData({data} );
  return true;
};
