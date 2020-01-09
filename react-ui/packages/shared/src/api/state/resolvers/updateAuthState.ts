// tslint:disable no-any

import { AuthenticationState } from '../../../types';
import { authStateQuery } from '../../graphql/state';

export const updateAuthenticationState = (_: any, input: any, { cache }: any) => {
  const { input: {authenticationState}} = input;

  if (!authenticationState) {
    throw new Error ('updateAuthenticationState: missing authenticationState');
  }

  const data = buildApolloCacheAuthState(authenticationState);

  cache.writeData({data} );

  return null;
};

export const buildAuthState = (authenticationState: AuthenticationState): any => {
  return {
      __typename: 'AuthState',
      authenticationState
   };
};

/**
 * Wrapper to update auth state outside of components
 */
export const writeAuthStateToCache = (cache: any, authenticationState: AuthenticationState) => {
  return updateAuthenticationState(null, {input: {authenticationState}}, {cache});
};

/**
 * Wrapper to read the auth state outside of components
 */
export const readAuthStateFromCache = (cache: any, logErrors: boolean = true) => {
  // Query the current authState from the link cache
  try {
    return cache.readQuery({query: authStateQuery});
  } catch (e) {
    if (logErrors) {
      // tslint:disable-next-line no-console
      console.error('Error reading authState from link store', e);
    }
    return null;
  }
};

export const buildApolloCacheAuthState = (authenticationState: AuthenticationState): any => {
  const authState = buildAuthState(authenticationState);
  return  { authState };
};
// tslint:enable no-any
