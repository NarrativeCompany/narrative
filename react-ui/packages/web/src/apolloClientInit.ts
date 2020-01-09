import { ApolloClient } from 'apollo-client';
import { RestLink } from 'apollo-link-rest';
import { ApolloLink } from 'apollo-link';
import {
  defaultDataIdFromObject,
  InMemoryCache,
  NormalizedCacheObject,
  IntrospectionFragmentMatcher
} from 'apollo-cache-inmemory';
import { onError } from 'apollo-link-error';
import { withClientState } from 'apollo-link-state';
import {
  defaults,
  ErrorType,
  HttpStatusCode,
  resolvers,
  setErrorStateinCache,
  RequestTerminatedException,
  reshapeErrorData,
  ValidationErrorException,
  readAuthStateFromCache,
  writeAuthStateToCache,
  API_URI,
  getIntrospectionQueryResultData
} from '@narrative/shared';
import { getAuthTokenState, removeAuthToken, setAuthTokenExpired2FA } from './shared/utils/authTokenUtils';
import { DefaultOptions } from 'apollo-client/ApolloClient';
import { Observable } from 'apollo-client/util/Observable';
import { LocationType } from './shared/utils/routeUtils';

const SINGLETON_ID = 'SINGLETON';
const DEBUG_MODE_KEY = 'debug';

export const debugMode = localStorage.getItem(DEBUG_MODE_KEY) === 'true';

const fragmentMatcher = new IntrospectionFragmentMatcher({
  introspectionQueryResultData: getIntrospectionQueryResultData()
});

// TODO: This is brittle.  Also where should this go? Sure would be nice to be able to annotate the schema instead.
// Use the exact type name as the enum value for this singleton mapping
export enum SingletonTypes {
  AuthState = 'AuthState',
  ErrorState = 'ErrorState'
}

// tslint:disable no-string-literal
export const apolloCache = new InMemoryCache({
  fragmentMatcher,
  // Implement a custom id generation strategy
  dataIdFromObject: object => {
    // Extract type name and OID if present
    const typename = object['__typename'];
    const objectOid = object['oid'];

    const key =
      typename &&
      objectOid &&
      `${typename}:${objectOid}`;

    if (key) {
      return key;
    }

    // Use the default id if found.  Default id is a field named 'id' or '_id' on the object
    const defaultId = defaultDataIdFromObject(object);
    if (defaultId) {
      return defaultId;
    }

    // Handle singleton types - use <typename>:SINGLETON as the cache key
    if (typename && SingletonTypes[typename]) {
      return `${typename}:${SINGLETON_ID}`;
    }

    // Let GraphQL generate a key that will end up nesting under the root if no id and not a singleton.  This works well
    // for paged queries etc. since the cache key name includes the query name and args so subsequent queries with
    // the same args overwrite previous values for the key.
    return null;
  }
});
// tslint:enable no-string-literal

/**
 * Error link to log all errors to the console and special handling for HTTP errors related to JWT and 5xx errors.
 */
const errorLink = onError(({graphQLErrors, networkError, operation, response}) => {
  // tslint:disable no-console
  // tslint:disable no-string-literal

  if (debugMode) {
    if (graphQLErrors) {
      console.error('graphql errors: ', JSON.stringify(graphQLErrors), graphQLErrors);
    }
    if (networkError) {
      console.error('network error: ', JSON.stringify(networkError), networkError);
    }
    if (graphQLErrors || networkError) {
      console.error('operation: ', JSON.stringify(operation), operation);
    }
    if (response) {
      console.error('response: ', JSON.stringify(response), response);
    }
  }

  // Handle errors not handled by special handling above
  return new Observable(observer => {

    // Special handling for certain HTTP errors
    if (networkError) {
      const statusCode = networkError['statusCode'] as HttpStatusCode;

      if (HttpStatusCode.BAD_REQUEST === statusCode) {
        switch (ErrorType[networkError['result']['type']]) {
          // Handle JWT related errors - there is no need to propagate a result to the client since both of these
          // cases result in either a log out or a modal 2FA dialog.  This is handled down stream by setAuthStateLink
          case ErrorType.JWT_INVALID:
            // When the user's JWT is invalid, remove the token from local storage which will log the user out
            console.info('User auth token has expired - removing from local storage');
            removeAuthToken();
            break;
          case ErrorType.JWT_2FA_EXPIRED:
            // When the user's JWT 2FA expires, set shared state indicating the token has an expired 2FA
            console.info('User auth token 2FA has expired - marking auth as 2FA expired');
            setAuthTokenExpired2FA(true);
            break;
          default:
            break;
        }
      }
    }

    const responseData =
      response &&
      response.data;

    // Reshape the error and hand off to the client as an exception or for display as a global error
    const errorResult = reshapeErrorData(responseData, graphQLErrors, networkError);

    if (errorResult.isValidationError()) {
      // Hand off to the client as validation error
      observer.error(new ValidationErrorException(errorResult.getValidationError()));
    } else {
      // Update the errorState in Apollo link state (cache)
      const store = operation.getContext().cache;
      setErrorStateinCache(store, errorResult.getErrorState());
      // Let the client know the error is being handled at a higher level but still give the client detail for use
      // when needed
      observer.error(new RequestTerminatedException(errorResult.getErrorState()));
    }
  });
  // tslint:enable no-console
  // tslint:enable no-string-literal
});

/**
 * ApolloLink for checking API server version in the header and comparing it to the SPA version.
 * If the versions are different, then reload the page so that the new SPA gets loaded.
 * Note that for local environments, the default 'local' value of NARRATIVE_VERSION will
 * cause the redirect to be skipped. To test locally, simply set the value of NARRATIVE_VERSION
 * in your browser console to something other than 'local'.
 *
 * Note that during a downtime deployment, the X-Narrative-Version header won't be present
 * on the response, in which case the reload will happen, which should load the auto-reloading
 * maintenance robot page.
 */
const apiVersionLink = new ApolloLink((operation, forward) => {
  if (!forward) {
    return null;
  }

  return forward(operation).map(response => {
    const { restResponses } = operation.getContext();
    if (restResponses && restResponses.length) {
      const { headers } = restResponses[0];
      if (headers) {
        // tslint:disable-next-line no-string-literal
        const spaVersion = window['NARRATIVE_VERSION'];
        const apiVersion = headers.get('x-narrative-version');
        if (spaVersion !== 'local') {
          if (!apiVersion || apiVersion !== spaVersion) {
            window.location.reload();
          }
        }
      }
    }

    return response;
  });
});

/**
 * Link that will modify the link cache authStateData when the authentication state changes
 */
const setAuthStateLink = new ApolloLink((operation, forward) => {
  if (!forward) {
    return null;
  }

  initializeAuthState();

  return forward(operation);
});

const stateLink = withClientState({
  cache: apolloCache,
  defaults,
  resolvers
});

const restLink = new RestLink({
  uri: API_URI,
  credentials: 'same-origin',
  typePatcher: {
    // tslint:disable no-any
    SearchPayload: (
      data: any,
    ) => {
      if (data && data.items) {
        data.items = data.items.map((item: any) => ({ __typename: item._type, ...item }));
      }

      return data;
    }
    // tslint:enable no-any
  }
});

/**
 * Default options for Apollo client - override in individual queries and mutations
 */
const defaultOptions: DefaultOptions = {
  watchQuery: {
    fetchPolicy: 'network-only'
  },
  query: {
    fetchPolicy: 'network-only'
  },
  mutate: {
  }
};

export const apolloClient: ApolloClient<NormalizedCacheObject> = new ApolloClient({
  link: ApolloLink.from(
    [ errorLink, setAuthStateLink, stateLink, apiVersionLink, restLink ]),
  cache: apolloCache,
  // do not commit this uncommented! we don't want it enabled in production environments.
  // connectToDevTools: true,
  defaultOptions
});

// initialize authenticationState on app load
export const initializeAuthState = (skipRead = false) => {
  const token = getAuthTokenState();
  const store = apolloClient.cache;

  // Query the current authState from the link cache
  let authStateData;
  if (!skipRead) {
    authStateData = readAuthStateFromCache(store, debugMode);
  }

  const authState =
    authStateData &&
    authStateData.authState;

  // Compare queried authState to the state calculated from local storage.  Only push an update to authState when the
  // state actually changes - this link will be called for every request and components only care about real
  // state changes
  if ( !(authState) || !(authState.authenticationState === token.getAuthenticationState()) ) {
    // Update the authenticationState in Apollo link state (cache)
    writeAuthStateToCache(apolloCache, token.getAuthenticationState());
  }
};

/**
 * Set up a callback that writes link state defaults to the store when the store is reset
 */
// TODO: #1036 put me back when underlying Apollo issues are resolved
// apolloClient.onResetStore(() => {
//   return new Promise((resolve, reject) => {
//     try {
//       stateLink.writeDefaults();
//       initializeAuthState();
//       resolve();
//     } catch (e) {
//       // tslint:disable-next-line no-console
//       logException('Error writing defaults to store', e);
//       reject(e);
//     }
//   });
// });

// TODO: #1036 Fix me when underlying Apollo issues are resolved
// zb: no need to writeDefaults anymore since we aren't calling resetStore() for the time being
/*
const writeDefaultsAndInitAuthState = () => {
  return new Promise((resolve) => {
    try {
      stateLink.writeDefaults();
      initializeAuthState(true);
      resolve();
    } catch (e) {
      logException('Error writing defaults to store', e);
      resolve();
    }
  });
};
*/

/**
 * Reset the Apollo store
 */
// TODO: #1036 Fix me when underlying Apollo issues are resolved
// zb: disabling resetStore so no one calls it by accident until this is fixed
// bl: as with logout() in authTokenUtils.ts, resetStore seems to be hanging / not finishing successfully in all
// cases. so, for now, we will bypass resetStore() and create an entirely new client & store instead.
// refer: #1519, #2344, #2785
/*export const resetStore = () => {
  return new Promise((resolve) => {
    return apolloCache.reset()
      .then(() => {
        apolloClient.resetStore()
          .then(async () => {
            await writeDefaultsAndInitAuthState();
            resolve();
          })
          .catch((exception) => {
            logException('Error resetting store', exception);
            resolve();
          });
      })
      .catch((exception) => {
        logException('Error resetting cache', exception);
        resolve();
      });
  });
};*/

export function reloadForLoginStateChange(to?: LocationType): void {
  if (to) {
    window.location.href = to;

  } else {
    window.location.reload();
  }
}

export function isOfType(object: {__typename: string}, typename: string): boolean {
  return object.__typename === typename;
}
