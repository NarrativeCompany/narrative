import * as H from 'history';
import { WebRoute } from '../constants/routes';
import { parseUrl, stringify as createQueryArgs } from 'query-string';
import { stripUndefinedProperties } from '@narrative/shared';
import { RouteComponentProps } from 'react-router';

export type LocationType = WebRoute | string;

/**
 * Resolve previous location from current location
 */
export const resolvePreviousLocation = (
  curLocation: H.Location,
  defaultRoute?: LocationType,
  currentRoute?: LocationType) =>
{
  // jw: came to this pretty late in the game, but I am unsure how the `state.from` is supposed to work... As far as I
  //     can tell it is never being set into the state via withRouter, and nowhere this component is used seems to add
  //     it in any way. Soo confused.
  const previousLocation = curLocation &&
    curLocation.state &&
    curLocation.state.from &&
    curLocation.state.from.pathname;

  if (!previousLocation) {
    return defaultRoute;
  }

  // jw: let's prevent redirecting back to ourselves
  if (currentRoute && previousLocation.startsWith(currentRoute)) {
    return defaultRoute;
  }

  return previousLocation;
};

// zb: this function simplifies the replacement and addition of new query parameters to an existing url
export function createUrl(
  baseUrl: string,
  params?: {},
  fragment?: string
): string {
  // jw: if neither a params or fragment were provided, then let's just return the baseUrl.
  if (!params && !fragment) {
    return baseUrl;
  }

  // jw: parse the domain and params out of the URL.
  const urlParts = parseUrl(baseUrl);

  // jw: first thing's first, need to merge the original params with our new params.
  const newParams = params
    // note: The order here is vital, because any params we want removed from the URL can be specified as undefined in
    //       our params input, and stripUndefinedProperties will remove them like magic.
    ? stripUndefinedProperties({...urlParts.query, ...params})
    // jw: since we were not provided params let's just use the original
    : urlParts.query;

  // jw: if we were given a fragment then let's go ahead and create a suffix we can just append to the URL
  const fragmentSuffix = fragment ? `#${fragment}` : '';

  // zb: if we have no query string properties, simply return the url
  if (Object.getOwnPropertyNames(newParams).length === 0) {
    return urlParts.url + fragmentSuffix;
  }

  // jw: since we have parameters, let's generate the new URL with the full set of data.
  return urlParts.url + '?' + createQueryArgs(newParams) + fragmentSuffix;
}

export function getIdForUrl (prettyUrlString: string | null, nicheOid: string): string {
  if (prettyUrlString) {
    return prettyUrlString;
  }
  return '_' + nicheOid;
}

const OID_PARAM_REGEX: RegExp = /^_[0-9]+/g;

export type IdRouteProps = RouteComponentProps<{id: string}>;

export function convertUrlIdToIdForApi(props: IdRouteProps): string {
  const { match: { params: { id } } } = props;

  const matcher = new RegExp(OID_PARAM_REGEX.source, OID_PARAM_REGEX.flags);

  if (matcher.test(id)) {
    return id.substr(1);
  } else {
    return getIdForApi(id);
  }
}

export function getIdForApi(id: string): string {
  return `id_${id}`;
}
