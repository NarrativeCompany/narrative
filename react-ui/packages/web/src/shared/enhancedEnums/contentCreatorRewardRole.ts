import { FormattedMessage } from 'react-intl';
import { ContentCreatorRewardRole } from '@narrative/shared';
import { EnumEnhancer } from '../utils/enhancedEnumUtils';
import { RewardsMessages } from '../i18n/RewardsMessages';

export class ContentCreatorRewardRoleHelper {
  role: ContentCreatorRewardRole;
  label?: FormattedMessage.MessageDescriptor;

  constructor(
    role: ContentCreatorRewardRole,
    label?: FormattedMessage.MessageDescriptor
  ) {
    this.role = role;
    this.label = label;
  }
}

const contentCreatorRewardRoleHelpers: {[key: number]: ContentCreatorRewardRoleHelper} = [];
contentCreatorRewardRoleHelpers[ContentCreatorRewardRole.WRITER] = new ContentCreatorRewardRoleHelper(
  ContentCreatorRewardRole.WRITER
);
contentCreatorRewardRoleHelpers[ContentCreatorRewardRole.PUBLICATION_OWNER] = new ContentCreatorRewardRoleHelper(
  ContentCreatorRewardRole.PUBLICATION_OWNER,
  RewardsMessages.ContentCreatorRewardRolePublicationOwner
);
contentCreatorRewardRoleHelpers[ContentCreatorRewardRole.PUBLICATION_ADMIN] = new ContentCreatorRewardRoleHelper(
  ContentCreatorRewardRole.PUBLICATION_ADMIN,
  RewardsMessages.ContentCreatorRewardRolePublicationAdmin
);
contentCreatorRewardRoleHelpers[ContentCreatorRewardRole.PUBLICATION_EDITOR] = new ContentCreatorRewardRoleHelper(
  ContentCreatorRewardRole.PUBLICATION_EDITOR,
  RewardsMessages.ContentCreatorRewardRolePublicationEditor
);

export const EnhancedContentCreatorRewardRole =
  new EnumEnhancer<ContentCreatorRewardRole, ContentCreatorRewardRoleHelper>(
  contentCreatorRewardRoleHelpers
);
