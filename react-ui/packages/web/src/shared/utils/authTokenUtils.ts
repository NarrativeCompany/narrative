import { AuthenticationState, logException } from '@narrative/shared';
import * as Cookies from 'es-cookie';
import { reloadForLoginStateChange } from '../../apolloClientInit';
import { LocationType } from './routeUtils';
export const tokenTimestampKey: string = '@narrative/token-lastUpdated';
export const expired2FAKey: string = '@narrative/token2FAExpired';
export const expired2FATimestampKey: string = '@narrative/token2FAExpired-lastUpdated';

const AUTH_COOKIE_NAME: string = 'Authorization';

/*
 * Class to encapsulate authentication token state
 */
export class AuthTokenState {
  token: string | null;
  tokenTimestamp: Date;
  expired2FA?: boolean;
  expired2FATimestamp: Date;

  constructor(token: string | null, tokenTimestamp: Date, expired2FA: boolean, expired2FATimestamp: Date) {
    this.token = token;
    this.tokenTimestamp = tokenTimestamp;
    this.expired2FA = expired2FA;
    this.expired2FATimestamp = expired2FATimestamp;
  }

  /*
   * Calculate AuthenticationState based on JWT token properties from local storage
   */
  getAuthenticationState (): AuthenticationState {
    if (!!(this.token)) {
      if (this.expired2FA) {
        return AuthenticationState.USER_REQUIRES_2FA;
      } else {
        return AuthenticationState.USER_AUTHENTICATED;
      }
    } else {
      return AuthenticationState.USER_NOT_AUTHENTICATED;
    }
  }
}

/** Set 2FA expired state in local storage for the current token
 *
 * @param expired true if 2FA is expired for the token, false otherwise
 */
export function setAuthTokenExpired2FA(expired: boolean) {
  localStorage.removeItem(expired2FAKey);
  localStorage.setItem(expired2FATimestampKey, new Date().toString());
  if (expired) {
    localStorage.setItem(expired2FAKey, 'true');
  }
}

function removeCookie () {
  Cookies.remove(AUTH_COOKIE_NAME);
}

/**
 * Store a JWT token in local storage
 *
 * @param token The token to store
 * @param expired2fa true if 2FA is expired for the token, false otherwise
 */
export function storeAuthToken (token: string, expired2fa: boolean) {
  if (!token) {
    throw new Error('storeAuthToken: no token provided');
  }

  try {
    removeCookie();
    // Set cookie with an expiration of one year from now which is the max "remember me" time for a JWT
    Cookies.set(AUTH_COOKIE_NAME, token, { expires: 365 });
  } catch (exception) {
    logException('Error setting auth cookie', exception);
  }

  try {
    localStorage.removeItem(tokenTimestampKey);
    localStorage.setItem(tokenTimestampKey, new Date().toString());
    setAuthTokenExpired2FA(expired2fa);
  } catch (exception) {
    logException('Error setting auth timestamps in local storage', exception);
  }
}

/*
 * Remove the JWT token from local storage
 */
export function removeAuthToken () {
  removeCookie();
  localStorage.setItem(tokenTimestampKey, new Date().toString());
  localStorage.removeItem(expired2FAKey);
  localStorage.setItem(expired2FATimestampKey, new Date().toString());
}

/*
 * Get the current AuthTokenState by pulling data from local storage
 */
export function getAuthTokenState (): AuthTokenState {
  return new AuthTokenState(
    Cookies.get(AUTH_COOKIE_NAME) || null,
    readDateFromStorage(tokenTimestampKey),
    isAuthTokenExpired2FA(),
    getAuthTokenExpired2FATimestamp());
}

/*
 * Determine if the current JWT's 2FA is expired
 */
export function isAuthTokenExpired2FA () {
  const expired2FA = localStorage.getItem(expired2FAKey);
  return expired2FA === 'true';
}

/**
 * Get the timestamp for when the 2FA state was last updated
 */
export function getAuthTokenExpired2FATimestamp () {
  return readDateFromStorage(expired2FATimestampKey);
}

/**
 * Read a date from local storage
 *
 * @param key The key to read from local storage as a date
 */
function readDateFromStorage(key: string): Date  {
  const dateStr = localStorage.getItem(key);
  return dateStr ? new Date(dateStr) : new Date(0);
}

/**
 * Wrap logout functionality so it can be shared
 *  TODO: Move me to an appropriate library
 */
export const logout = async (to?: LocationType) => {
  removeAuthToken();
  // bl: resetStore seems to be hanging / not finishing successfully in all cases. so, for now,
  // let's skip the resetStore() and just reload the full page as a workaround for logout.
  // refer: #1519, #2344, #2785
  // Reset the Apollo store
  // await resetStore();
  // zb: Just reloading the page after removing the auth token
  // is the easiest and cleanest solution
  reloadForLoginStateChange(to);
};
