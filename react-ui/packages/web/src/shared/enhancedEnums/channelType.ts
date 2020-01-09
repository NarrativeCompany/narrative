import { ChannelType } from '@narrative/shared';
import { EnumEnhancer } from '../utils/enhancedEnumUtils';
import { FormattedMessage } from 'react-intl';
import { ChannelMessages } from '../i18n/ChannelMessages';

export class ChannelTypeHelper {
  type: ChannelType;
  deletedChannelMessage: FormattedMessage.MessageDescriptor;
  ownerMessage: FormattedMessage.MessageDescriptor;

  constructor(
    type: ChannelType,
    deletedChannelMessage: FormattedMessage.MessageDescriptor,
    ownerMessage: FormattedMessage.MessageDescriptor
  ) {
    this.type = type;
    this.deletedChannelMessage = deletedChannelMessage;
    this.ownerMessage = ownerMessage;
  }

  isNiche() {
    return this.type === ChannelType.NICHE;
  }

  isPublication() {
    return this.type === ChannelType.PUBLICATION;
  }

  isPersonalJournal() {
    return this.type === ChannelType.PERSONAL_JOURNAL;
  }
}

const typeHelpers: {[key: number]: ChannelTypeHelper} = [];
typeHelpers[ChannelType.NICHE] = new ChannelTypeHelper(
  ChannelType.NICHE,
  ChannelMessages.DeletedNiche,
  ChannelMessages.NicheOwner,
);
typeHelpers[ChannelType.PUBLICATION] = new ChannelTypeHelper(
  ChannelType.PUBLICATION,
  ChannelMessages.DeletedPublication,
  ChannelMessages.PublicationOwner
);
typeHelpers[ChannelType.PERSONAL_JOURNAL] = new ChannelTypeHelper(
  ChannelType.PERSONAL_JOURNAL,
  ChannelMessages.DeletedPersonalJournal,
  ChannelMessages.PersonalJournalOwner,
);

export const EnhancedChannelType = new EnumEnhancer<ChannelType, ChannelTypeHelper>(
  typeHelpers
);
