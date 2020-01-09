import * as React from 'react';
import { compose, withHandlers, withProps } from 'recompose';
import {
  getEnumLookupObject,
  handleFormlessServerOperation,
  Publication,
  PublicationPowerUsers,
  PublicationRole,
  User,
  withDeletePublicationPowerUser,
  WithDeletePublicationPowerUserProps,
  withState,
  WithStateProps
} from '@narrative/shared';
import {
  WithCurrentUserProps,
  withExtractedCurrentUser
} from '../../../../../shared/containers/withExtractedCurrentUser';
import { RemovePowerUserModal, RemovePowerUserModalProps } from './RemovePowerUserModal';
import { InjectedIntlProps, injectIntl } from 'react-intl';
import { openNotification } from '../../../../../shared/utils/notificationsUtil';
import { PublicationDetailsMessages } from '../../../../../shared/i18n/PublicationDetailsMessages';
import { EnhancedPublicationRole } from '../../../../../shared/enhancedEnums/publicationRole';
import { PowerUsersRoleSection } from './PowerUsersRoleSection';
import {
  handlePowerUserChangeForCurrentUser,
  PublicationRoleLookupType
} from '../../../../../shared/utils/publicationUtils';
import { RouteComponentProps, withRouter } from 'react-router';

export interface PowerUsersRoleSectionsHandlers {
  openRemoveUserModal: (user: User, role: PublicationRole) => void;
  openRemoveSelfModal: (role: PublicationRole) => void;
}

// jw: there is at least one more handler we need internally that I do not want to expose.
interface Handlers extends PowerUsersRoleSectionsHandlers {
  onRemoveUserConfirmed: () => void;
}

interface State {
  removeUser?: User;
  removeFromRole?: PublicationRole;
  removeSelf?: boolean;
  processingRemoval?: boolean;
}

interface ParentProps {
  invitableRoleLookup: PublicationRoleLookupType;
  publicationPowerUsers: PublicationPowerUsers;
  publication: Publication;
  viewedByOwner: boolean;
}

interface Props extends ParentProps, PowerUsersRoleSectionsHandlers {
  manageableRoleLookup: PublicationRoleLookupType;
  removeUserModalProps: RemovePowerUserModalProps;
}

const PowerUsersRoleSectionsComponent: React.SFC<Props> = (props) => {
  const {
    manageableRoleLookup,
    publicationPowerUsers,
    publication,
    openRemoveUserModal,
    openRemoveSelfModal,
    removeUserModalProps,
    viewedByOwner
  } = props;

  const sharedProps = {
    manageableRoleLookup,
    openRemoveUserModal,
    openRemoveSelfModal,
    publication,
    viewedByOwner,
  };

  return (
    <React.Fragment>
      <PowerUsersRoleSection
        title={PublicationDetailsMessages.AdminsTitle}
        description={PublicationDetailsMessages.AdminsDescription}
        role={PublicationRole.ADMIN}
        users={publicationPowerUsers.admins}
        invitedUsers={publicationPowerUsers.invitedAdmins}
        {...sharedProps}
      />

      <PowerUsersRoleSection
        title={PublicationDetailsMessages.EditorsTitle}
        description={PublicationDetailsMessages.EditorsDescription}
        role={PublicationRole.EDITOR}
        users={publicationPowerUsers.editors}
        invitedUsers={publicationPowerUsers.invitedEditors}
        limit={publicationPowerUsers.editorLimit}
        {...sharedProps}
      />

      <PowerUsersRoleSection
        title={PublicationDetailsMessages.WritersTitle}
        description={PublicationDetailsMessages.WritersDescription}
        role={PublicationRole.WRITER}
        users={publicationPowerUsers.writers}
        invitedUsers={publicationPowerUsers.invitedWriters}
        limit={publicationPowerUsers.writerLimit}
        {...sharedProps}
      />

      <RemovePowerUserModal {...removeUserModalProps} />
    </React.Fragment>
  );
};

type HandlerProps = ParentProps &
  WithCurrentUserProps &
  InjectedIntlProps &
  WithStateProps<State> &
  WithDeletePublicationPowerUserProps &
  RouteComponentProps;

export const PowerUsersRoleSections = compose(
  // jw: we will need to get the current user in order to fulfill the openRemoveSelfModal call.
  withExtractedCurrentUser,
  withDeletePublicationPowerUser,
  injectIntl,
  withState<State>({}),
  withRouter,
  withHandlers<HandlerProps, Handlers>({
    openRemoveUserModal: (props) => (removeUser: User, removeFromRole: PublicationRole) => {
      const { setState } = props;

      setState(ss => ({
        ...ss,
        removeUser,
        removeFromRole,
        removeSelf: undefined,
        processingRemoval: undefined
      }));
    },
    openRemoveSelfModal: (props) => (removeFromRole: PublicationRole) => {
      const { setState } = props;
      const removeUser = props.currentUser;

      setState(ss => ({
        ...ss,
        removeUser,
        removeFromRole,
        removeSelf: true,
        processingRemoval: undefined
      }));
    },
    onRemoveUserConfirmed: (props) => async () => {
      const {
        state: { removeUser, removeFromRole, removeSelf },
        setState,
        deletePublicationPowerUser,
        viewedByOwner,
        intl: { formatMessage },
        history
      } = props;

      if (!removeUser || !removeFromRole) {
        // todo:error-handling: This should never come up, so how did it? Should report to server.
        return;
      }

      const publicationOid = props.publicationPowerUsers.oid;

      setState(ss => ({...ss, processingRemoval: true}));

      let result: PublicationPowerUsers | undefined | null;
      try {
        result = await handleFormlessServerOperation(() => deletePublicationPowerUser({
          publicationOid,
          userOid: removeUser.oid,
          role: removeFromRole
        }));

        // jw: if an error prevented us from getting a result above them we should short out here.
        if (!result) {
          return;
        }

        const roleType = EnhancedPublicationRole.get(removeFromRole);
        const roleNameWithArticle = formatMessage(roleType.nameWithArticle);

        // jw: Let's see if we need to redirect the user back to the home page with a message.
        const redirected = await handlePowerUserChangeForCurrentUser(
          viewedByOwner,
          result.publicationDetail,
          history,
          formatMessage,
          PublicationDetailsMessages.NoLongerHavePowerUserAccessDueToPowerUserRemoval,
          {roleNameWithArticle}
        );
        if (redirected) {
          // jw: if we are redirecting we do not want the state to be updated below.
          result = undefined;
          return;
        }
        // jw: since the user should still have access to the page let's give them a message and let apollo trigger
        //     a re-render.
        await openNotification.updateSuccess(
          {
            description: '',
            message: formatMessage(
              removeSelf
                ? PublicationDetailsMessages.RemoveSelfSuccess
                : PublicationDetailsMessages.RemoveUserSuccess
              , {roleNameWithArticle}
            ),
            duration: 5
          });

      } finally {
        if (result) {
          setState(ss => ({
            ...ss,
            removeUser: undefined,
            removeFromRole: undefined,
            removeSelf: undefined,
            processingRemoval: undefined
          }));

        } else {
          setState(ss => ({...ss, processingRemoval: undefined}));
        }
      }
    }
  }),
  withProps<Pick<Props, 'manageableRoleLookup'>, ParentProps>(
    (props: ParentProps): Pick<Props, 'manageableRoleLookup'> => {
      const { publicationPowerUsers: { currentUserCanManageRoles } } = props;

      const manageableRoleLookup = getEnumLookupObject(currentUserCanManageRoles) as PublicationRoleLookupType;

      return { manageableRoleLookup };
    }
  ),
  withProps<Pick<Props, 'removeUserModalProps'>, HandlerProps & Handlers>((props: HandlerProps & Handlers) => {
    const { setState, state } = props;

    const user = state.removeUser;
    const role = state.removeFromRole;
    const removingSelf = state.removeSelf;
    const processing = state.processingRemoval;
    const removeConfirmed = props.onRemoveUserConfirmed;

    return {
      removeUserModalProps: {
        user,
        role,
        removingSelf,
        processing,
        removeConfirmed,
        close: () => {
          setState(ss => ({
            ...ss,
            removeUser: undefined,
            removeFromRole: undefined,
            removeSelf: undefined,
            processingRemoval: undefined
          }));
        }
      }
    };
  })
)(PowerUsersRoleSectionsComponent) as React.ComponentClass<ParentProps>;
