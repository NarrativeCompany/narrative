import { WebRoute } from '../constants/routes';
import { getIdForUrl } from './routeUtils';
import { generatePath } from 'react-router';
import { Niche, Publication } from '@narrative/shared';
import { isOfType } from '../../apolloClientInit';

export type Channel = Niche | Publication;

export function getChannelUrl (channel: Channel, route?: string): string {
  if (!route) {
    if (isOfType(channel, 'Niche')) {
      route = WebRoute.NicheDetails;
    } else {
      // todo:error-handling assert that the type of channel is Publication
      route = WebRoute.PublicationDetails;
    }
  }
  return getChannelUrlByValue(channel.prettyUrlString, channel.oid, route);
}

export function getChannelUrlByValue (prettyUrlString: string | null, oid: string, route: string): string {
  const id = getIdForUrl(prettyUrlString, oid);
  return generatePath(route, { id });
}
