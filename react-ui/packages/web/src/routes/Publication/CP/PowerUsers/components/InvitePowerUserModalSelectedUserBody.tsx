import * as React from 'react';
import { compose, Omit, withProps } from 'recompose';
import {
  User,
  withPublicationPowerUser,
  WithPublicationPowerUsersParentProps,
  WithPublicationPowerUserProps,
  PublicationRole,
  getEnumLookupObject
} from '@narrative/shared';
import { InvitePowerUserButtonProps } from './InvitePowerUserButton';
import { withLoadingPlaceholder } from '../../../../../shared/utils/withLoadingPlaceholder';
import { PublicationRoleLookupType } from '../../../../../shared/utils/publicationUtils';
import { Block } from '../../../../../shared/components/Block';
import { PublicationDetailsMessages } from '../../../../../shared/i18n/PublicationDetailsMessages';
import { MemberLink } from '../../../../../shared/components/user/MemberLink';
import { Link } from '../../../../../shared/components/Link';
import { FormattedMessage } from 'react-intl';
import { Icon } from '../../../../../shared/components/Icon';
import { Paragraph } from '../../../../../shared/components/Paragraph';
import { InvitePowerUserForm } from './InvitePowerUserForm';

interface ParentProps extends Omit<InvitePowerUserButtonProps, 'currentUserRoles'> {
  selectedUser: User;
  unselectUser: () => void;
  close: () => void;
}

interface Props extends ParentProps {
  userRoles: PublicationRole[];
  selectableRoles: PublicationRole[];
}

const InvitePowerUserModalSelectedUserBodyComponent: React.SFC<Props> = (props) => {
  const { selectedUser, unselectUser, selectableRoles, userRoles, publicationDetail, close } = props;

  const userLink = <MemberLink user={selectedUser} hideBadge={true} targetBlank={true} appendUsername={true} />;

  return (
    <React.Fragment>
      <Paragraph marginBottom="large">
        {userLink}
        <Link.Anchor onClick={unselectUser} color="dark" noHoverEffect={true} style={{marginLeft: 10}}>
          <Icon type="close-circle" />
        </Link.Anchor>
      </Paragraph>

      {!selectableRoles.length
        ? <Block>
            <FormattedMessage {...PublicationDetailsMessages.SelectedUserAlreadyInAllRoles} values={{userLink}}/>
          </Block>
        : <InvitePowerUserForm
            publicationDetail={publicationDetail}
            selectedUser={selectedUser}
            selectableRoles={selectableRoles}
            onUserInvited={close}
            userRoles={userRoles}
          />
      }
    </React.Fragment>
  );
};

export const InvitePowerUserModalSelectedUserBody = compose(
  withProps<WithPublicationPowerUsersParentProps, ParentProps>((props: ParentProps) => {
    const publicationOid = props.publicationDetail.oid;
    const userOid = props.selectedUser.oid;

    return { publicationOid, userOid };
  }),
  withPublicationPowerUser,
  // jw: we just want the inline loading placeholder to indicate that something is happening.
  withLoadingPlaceholder(),
  withProps<Pick<Props, 'selectableRoles' | 'userRoles'>, WithPublicationPowerUserProps & ParentProps>(
    (props: WithPublicationPowerUserProps & ParentProps): Pick<Props, 'selectableRoles' | 'userRoles'> => {

      const { canInviteRoles, publicationPowerUser: { roles } } = props;
      // jw: to ease detecting which roles the user already has let's create a lookup object.
      const rolesLookup = getEnumLookupObject(roles) as PublicationRoleLookupType;

      // jw: now that we have the lookup filtering the roles is really simple.
      const selectableRoles = canInviteRoles.filter(role => !rolesLookup[role]);

      return { selectableRoles, userRoles: roles };
    }
  )
)(InvitePowerUserModalSelectedUserBodyComponent) as React.ComponentClass<ParentProps>;
