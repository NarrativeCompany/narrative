import * as React from 'react';
import { PublicationUserAssociation } from '@narrative/shared';
import { FormattedMessage } from 'react-intl';
import { MemberChannelsMessages } from '../../../../../shared/i18n/MemberChannelsMessages';
import { EnhancedPublicationRole } from '../../../../../shared/enhancedEnums/publicationRole';
import { associatedChannelAvatarSize, MemberAssociatedChannel } from '../../components/MemberAssociatedChannel';
import { PublicationAvatar } from '../../../../../shared/components/publication/PublicationAvatar';

interface Props {
  association: PublicationUserAssociation;
}

export const MemberAssociatedPublication: React.SFC<Props> = (props) => {
  const { association: { publication, roles, owner } } = props;

  const roleNames: React.ReactNode[] = [];
  if (owner) {
    roleNames.push(<FormattedMessage {...MemberChannelsMessages.OwnerRoleName}/>);
  }
  roles.forEach(role => {
    const roleType = EnhancedPublicationRole.get(role);

    roleNames.push(<FormattedMessage {...roleType.name}/>);
  });

  return (
    <MemberAssociatedChannel
      channel={publication}
      subTitle={publication.description}
      description={roleNames.map((roleName, index) => (
        <React.Fragment key={index}>
          {index > 0 ? ', ' : undefined}
          {roleName}
        </React.Fragment>
      ))}
      avatar={<PublicationAvatar publication={publication} size={associatedChannelAvatarSize} />}
    />
  );
};
