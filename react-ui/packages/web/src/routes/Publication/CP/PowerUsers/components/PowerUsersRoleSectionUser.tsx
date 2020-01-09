import * as React from 'react';
import { PowerUsersRoleSectionProps } from './PowerUsersRoleSection';
import { User } from '@narrative/shared';
import { Icon } from '../../../../../shared/components/Icon';
import { Link } from '../../../../../shared/components/Link';
import { PowerUserRow } from './PowerUserRow';

interface Props extends Pick<
  PowerUsersRoleSectionProps,
  'currentUser' | 'role' | 'manageableRoleLookup' | 'openRemoveUserModal' | 'openRemoveSelfModal'
> {
  user: User;
  pending?: boolean;
}

export const PowerUsersRoleSectionUser: React.SFC<Props> = (props) => {
  const { user, currentUser, pending, role, manageableRoleLookup, openRemoveUserModal, openRemoveSelfModal } = props;

  let tool: React.ReactNode | undefined;
  // jw: regardless if the user can invite this role, if they are this user then just use the remove self modal.
  if (user.oid === currentUser.oid) {
    tool = (
      <Link.Anchor onClick={() => openRemoveSelfModal(role)} color="dark" noHoverEffect={true}>
        <Icon type="delete" />
      </Link.Anchor>
    );
  } else if (manageableRoleLookup[role]) {
    tool = (
      <Link.Anchor onClick={() => openRemoveUserModal(user, role)} color="dark" noHoverEffect={true}>
        <Icon type="delete" />
      </Link.Anchor>
    );
  }

  return <PowerUserRow user={user} pending={pending} tool={tool} />;
};
