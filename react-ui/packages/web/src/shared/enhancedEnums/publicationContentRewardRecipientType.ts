import { FormattedMessage } from 'react-intl';
import { PublicationContentRewardRecipientType } from '@narrative/shared';
import { EnumEnhancer } from '../utils/enhancedEnumUtils';
import { PublicationDetailsMessages } from '../i18n/PublicationDetailsMessages';

export class PublicationContentRewardRecipientTypeHelper {
  type: PublicationContentRewardRecipientType;
  name: FormattedMessage.MessageDescriptor;
  shortName: FormattedMessage.MessageDescriptor;

  constructor(
    type: PublicationContentRewardRecipientType,
    name: FormattedMessage.MessageDescriptor,
    shortName: FormattedMessage.MessageDescriptor
  ) {
    this.type = type;
    this.name = name;
    this.shortName = shortName;
  }
}

const publicationContentRewardRecipientTypeHelpers: {[key: number]: PublicationContentRewardRecipientTypeHelper} = [];
publicationContentRewardRecipientTypeHelpers[PublicationContentRewardRecipientType.OWNER]
  = new PublicationContentRewardRecipientTypeHelper(
  PublicationContentRewardRecipientType.OWNER,
  PublicationDetailsMessages.PublicationOwner,
  PublicationDetailsMessages.PublicationOwner
);
publicationContentRewardRecipientTypeHelpers[PublicationContentRewardRecipientType.ADMINS]
  = new PublicationContentRewardRecipientTypeHelper(
  PublicationContentRewardRecipientType.ADMINS,
  PublicationDetailsMessages.AllAdmins,
  PublicationDetailsMessages.Admins
);
publicationContentRewardRecipientTypeHelpers[PublicationContentRewardRecipientType.EDITORS]
  = new PublicationContentRewardRecipientTypeHelper(
  PublicationContentRewardRecipientType.EDITORS,
  PublicationDetailsMessages.AllEditors,
  PublicationDetailsMessages.Editors
);

export const EnhancedPublicationContentRewardRecipientType =
  new EnumEnhancer<PublicationContentRewardRecipientType, PublicationContentRewardRecipientTypeHelper>(
  publicationContentRewardRecipientTypeHelpers
);
