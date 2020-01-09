import { FormattedMessage } from 'react-intl';

// tslint:disable-next-line no-any
export function isIntlMessageDescriptor(object: any): object is FormattedMessage.MessageDescriptor {
  // jw: if the object is not, well, an object, then let's short out.
  if (!object || (typeof object) !== 'object') {
    return false;
  }
  // jw: technically defaultMessage is optional but we always define it, so let's use that as a discriminator also
  return 'id' in object && 'defaultMessage' in object;
}
