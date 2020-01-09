import * as React from 'react';
import { compose, withProps } from 'recompose';
import { WithPublicationDetailsContextProps } from '../../../components/PublicationDetailsContext';
import {
  User,
  withPublicationPowerUsers,
  WithPublicationPowerUsersParentProps,
  WithPublicationPowerUsersProps,
  getEnumLookupObject
} from '@narrative/shared';
import { Heading } from '../../../../../shared/components/Heading';
import { FormattedMessage } from 'react-intl';
import { PublicationDetailsMessages } from '../../../../../shared/i18n/PublicationDetailsMessages';
import { PowerUsersDescription } from './PowerUsersDescription';
import { fullPlaceholder, withLoadingPlaceholder } from '../../../../../shared/utils/withLoadingPlaceholder';
import { PowerUsersOwnerSection } from './PowerUsersOwnerSection';
import { PowerUsersRoleSections } from './PowerUsersRoleSections';
import { PublicationRoleLookupType } from '../../../../../shared/utils/publicationUtils';

type Props = WithPublicationDetailsContextProps &
  WithPublicationDetailsContextProps &
  WithPublicationPowerUsersProps & {
    invitableRoleLookup: PublicationRoleLookupType;
  };

const PowerUsersBodyComponent: React.SFC<Props> = (props) => {
  const { publicationDetail, publicationPowerUsers, invitableRoleLookup, currentUserRoles } = props;
  const { currentUserAllowedInviteRoles } = publicationPowerUsers;

  return (
    <React.Fragment>
      <Heading size={2}>
        <FormattedMessage {...PublicationDetailsMessages.PowerUsers} />
      </Heading>

      <PowerUsersDescription
        publicationDetail={publicationDetail}
        canInviteRoles={currentUserAllowedInviteRoles}
        currentUserRoles={currentUserRoles}
      />

      {publicationDetail.owner &&
        <PowerUsersOwnerSection
          owner={publicationDetail.owner as User}
          publicationDetail={publicationDetail}
          publicationPowerUsers={publicationPowerUsers}
        />
      }

      <PowerUsersRoleSections
        publicationPowerUsers={publicationPowerUsers}
        publication={publicationDetail.publication}
        invitableRoleLookup={invitableRoleLookup}
        viewedByOwner={currentUserRoles.owner}
      />
    </React.Fragment>
  );
};

export const PowerUsersBody = compose(
  withProps<WithPublicationPowerUsersParentProps, WithPublicationDetailsContextProps>(
    (props: WithPublicationDetailsContextProps) => {
      const publicationOid = props.publicationDetail.oid;

      return { publicationOid };
    }
  ),
  withPublicationPowerUsers,
  withLoadingPlaceholder(fullPlaceholder),
  withProps<Pick<Props, 'invitableRoleLookup'>, Props>((props: Props) => {
    // jw: to make looking up what roles the viewer can invite, let's create a lookup object that defines them as true
    const invitableRoleLookup =
      getEnumLookupObject(props.publicationPowerUsers.currentUserAllowedInviteRoles) as PublicationRoleLookupType;

    return { invitableRoleLookup };
  })
)(PowerUsersBodyComponent) as React.ComponentClass<WithPublicationDetailsContextProps>;
