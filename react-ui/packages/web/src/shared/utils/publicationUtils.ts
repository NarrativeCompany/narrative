import { PublicationRole, PublicationDetail } from '@narrative/shared';
import * as H from 'history';
import { generatePath } from 'react-router';
import { FormattedMessage, MessageValue } from 'react-intl';
import { openNotification } from './notificationsUtil';
import { WebRoute } from '../constants/routes';
import { createUrl } from './routeUtils';

export const nicheParam = 'niche';

export const createNicheFilteredContentStreamUrl = (urlBase: string, nicheOid: string): string => {
  return createUrl(urlBase, {[nicheParam]: nicheOid});
};

type MessageDescriptor = FormattedMessage.MessageDescriptor;

export type PublicationRoleLookupType = {[key in keyof typeof PublicationRole]: boolean};

// jw: there are a couple of places where we will need to check a user has access
export async function handlePowerUserChangeForCurrentUser(
  isOwner: boolean,
  publicationDetail: PublicationDetail,
  history: H.History,
  formatMessage: (messageDescriptor: MessageDescriptor, values?: {[key: string]: MessageValue}) => string,
  message: MessageDescriptor,
  messageValues?: {[key: string]: MessageValue}
): Promise<boolean> {
  // jw: if the viewer is the owner or they possess any roles then we should allow them through.
  if (isOwner || publicationDetail.currentUserRoles.length) {
    return false;
  }

  // jw: otherwise, we need to redirect the user to the pain page of the publication with a message explaining why.
  await openNotification.updateSuccess(
    {
      description: '',
      message: formatMessage(message, messageValues),
      duration: 0
    });

  // jw: now do the redirect
  const id = publicationDetail.publication.prettyUrlString;
  history.push(generatePath(WebRoute.PublicationDetails, {id}));

  return true;
}
