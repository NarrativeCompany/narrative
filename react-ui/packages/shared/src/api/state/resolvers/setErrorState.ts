// tslint:disable no-any

import { errorStateQuery } from '../../graphql/error';
import { isGraphQLObjectEmpty, TYPENAME_FIELD_NAME } from '../../../utils';
import { ErrorState } from '../../../types';

/**
 * Set the error state.  This function will only set the state if it isn't already set.
 */
export const setErrorState = (_: any, input: any, { cache }: any) => {
  const newErrorState =
    input &&
    input.input &&
    input.input.errorState;

  if (!newErrorState) {
    throw new Error ('updateErrorState: missing errorState');
  }

  let prevCacheEntry;
  try {
    prevCacheEntry = cache.readQuery({query: errorStateQuery});
  } catch (e) {
    // tslint:disable-next-line no-console
    console.error('Error reading previous error state from cache', e.stack || e);
  }

  const prevErrorState =
    prevCacheEntry &&
    prevCacheEntry.errorState;

  // If the previous state is "empty" then write the new state.  Empty is as good as it gets with GraphQL objects in
  // the cache because there is no way to invalidate an entry in the cache :/
  if (!prevErrorState || prevErrorState && isGraphQLObjectEmpty(prevErrorState)) {
    const data = buildApolloCacheErrorState(newErrorState);
    cache.writeData({data} );
    return true;
  } else {
    // tslint:disable-next-line no-console
    console.info('Did not write new error state - already present in cache - new value: ', newErrorState);
    return false;
  }
};

/**
 * Wrapper to set error state outside of components
 */
export const setErrorStateinCache = (cache: any, errorState: ErrorState) => {
  return setErrorState(null, {input: {errorState}}, {cache});
};

export const buildEmptyErrorState = (): ErrorState => {
  return {
    type: null,
    title: null,
    message: null,
    referenceId: null,
    detail: null,
    httpStatusCode: null,
    result: null,
    data: null,
    graphQLErrors: null,
    stack: null,
    [TYPENAME_FIELD_NAME]: 'ErrorState'
  };
};

export const buildErrorState = (errorState: ErrorState): any => {
  // Start with an empty object and spread the errorState fields on top of the empty object.  Apollo link state gets
  // unhappy when you don't have every field present in the object to be persisted in the cache.
  const empty = buildEmptyErrorState();

  // Only write field values that are not 'unassigned'.  Empty fields should be null to keep Apollo state happy
  return Object.assign({...empty}, errorState);
};

export const buildApolloCacheErrorState = (errorStateParam: ErrorState): any => {
  const errorState = buildErrorState(errorStateParam);
  return  { errorState };
};
// tslint:enable no-any
