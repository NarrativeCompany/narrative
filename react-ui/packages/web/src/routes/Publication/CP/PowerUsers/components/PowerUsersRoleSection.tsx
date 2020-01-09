import * as React from 'react';
import { PublicationRole, User, Publication } from '@narrative/shared';
import { compose } from 'recompose';
import {
  WithCurrentUserProps,
  withExtractedCurrentUser
} from '../../../../../shared/containers/withExtractedCurrentUser';
import { Section } from '../../../../../shared/components/Section';
import { LocalizedNumber } from '../../../../../shared/components/LocalizedNumber';
import { PowerUsersRoleSectionUser } from './PowerUsersRoleSectionUser';
import { PowerUsersRoleSectionsHandlers } from './PowerUsersRoleSections';
import { PowerUserRoleLimitReachedWarning } from './PowerUserRoleLimitReachedWarning';
import { FormattedMessage } from 'react-intl';
import { PublicationRoleLookupType } from '../../../../../shared/utils/publicationUtils';

interface ParentProps extends PowerUsersRoleSectionsHandlers {
  publication: Publication;
  title: FormattedMessage.MessageDescriptor;
  // jw: note that all of the descriptions are assumed to have a userCount variable.
  description: FormattedMessage.MessageDescriptor;
  role: PublicationRole;
  manageableRoleLookup: PublicationRoleLookupType;
  users: User[];
  invitedUsers: User[];
  limit?: number;
  viewedByOwner: boolean;
}

export type PowerUsersRoleSectionProps = ParentProps &
  Pick<WithCurrentUserProps, 'currentUser'>;

const PowerUsersRoleSectionComponent: React.SFC<PowerUsersRoleSectionProps> = (props) => {
  const {
    title,
    description,
    role,
    manageableRoleLookup,
    users,
    invitedUsers,
    openRemoveUserModal,
    openRemoveSelfModal,
    currentUser,
    limit,
    publication,
    viewedByOwner
  } = props;

  const totalUsers = users.length + invitedUsers.length;
  const userCount = <LocalizedNumber value={totalUsers} />;
  const sharedSectionUserProps = { currentUser, role, manageableRoleLookup, openRemoveUserModal, openRemoveSelfModal };

  return (
    <Section
      title={<FormattedMessage {...title} values={{userCount}}/>}
      description={<FormattedMessage {...description} />}
    >
      {/* jw: first we need to output the users who have accepted their invitations */}
      {users.map(user =>
        <PowerUsersRoleSectionUser
          key={`powerUser_${role}_${user.oid}`}
          user={user}
          {...sharedSectionUserProps}
        />
      )}
      {/* jw: next, let's output the users who have yet to accept their invitations */}
      {invitedUsers.map(user =>
        <PowerUsersRoleSectionUser
          key={`invitedPowerUser_${role}_${user.oid}`}
          user={user}
          pending={true}
          {...sharedSectionUserProps}
        />
      )}
      {limit === totalUsers &&
        <PowerUserRoleLimitReachedWarning
          publication={publication}
          viewedByOwner={viewedByOwner}
          role={role}
        />
      }
    </Section>
  );
};

export const PowerUsersRoleSection = compose(
  withExtractedCurrentUser
)(PowerUsersRoleSectionComponent) as React.ComponentClass<ParentProps>;
