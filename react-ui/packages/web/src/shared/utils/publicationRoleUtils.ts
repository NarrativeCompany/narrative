import { EnhancedPublicationRole } from '../enhancedEnums/publicationRole';
import { PublicationDetail, User } from '@narrative/shared';

export interface PublicationRoleBooleans {
  owner: boolean;
  admin: boolean;
  editor: boolean;
  writer: boolean;
}

export function getPublicationRoleBooleans(
  publicationDetail: PublicationDetail,
  currentUser?: User,
): PublicationRoleBooleans {
  const roles: PublicationRoleBooleans = {
    owner: false,
    admin: false,
    editor: false,
    writer: false
  };
  if (currentUser) {
    const owner = publicationDetail.owner;
    roles.owner = !!owner && currentUser.oid === owner.oid;

    publicationDetail.currentUserRoles.forEach(role => {
      const roleType = EnhancedPublicationRole.get(role);
      roleType.setRoleBoolean(roles);
    });
  }
  return roles;
}
