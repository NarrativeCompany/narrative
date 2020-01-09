import { WebRoute } from '../constants/routes';
import { getChannelUrlByValue } from './channelUtils';

export function getNicheUrlByValue (
  prettyUrlString: string | null,
  nicheOid: string,
  route: string = WebRoute.NicheDetails
): string {
  return getChannelUrlByValue(prettyUrlString, nicheOid, route);
}
