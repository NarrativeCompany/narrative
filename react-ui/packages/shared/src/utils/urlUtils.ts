import { parse as parseQueryArgs } from 'query-string';

export function getQueryArg(query: string, param: string): string | undefined {
  const queryArgs = parseQueryArgs(query);
  if (queryArgs) {
    const paramValue = queryArgs[param];
    if (paramValue) {
      if (Array.isArray(paramValue)) {
        return paramValue[0];
      }

      return paramValue;
    }
  }
  return undefined;
}

export const getBaseUrl = (): string | undefined => {
  if (window
    && 'location' in window
    && 'protocol' in window.location
    && 'host' in window.location
  ) {
    return window.location.protocol + '//' + window.location.host;
  }
  return;
};
