import * as React from 'react';
import { compose, Omit, withHandlers, withProps } from 'recompose';
import {
  User,
  PublicationRole,
  PublicationDetail,
  withState,
  WithStateProps,
  getEnumLookupObject,
  withInvitePublicationPowerUser,
  WithInvitePublicationPowerUserProps,
  handleFormlessServerOperation
} from '@narrative/shared';
import { PublicationRoleLookupType } from '../../../../../shared/utils/publicationUtils';
import { FormControl } from '../../../../../shared/components/FormControl';
import { PublicationDetailsMessages } from '../../../../../shared/i18n/PublicationDetailsMessages';
import { FormattedMessage, InjectedIntlProps, injectIntl } from 'react-intl';
import { EnhancedPublicationRole } from '../../../../../shared/enhancedEnums/publicationRole';
import { Button } from '../../../../../shared/components/Button';
import { Checkbox } from 'antd';
import { Block } from '../../../../../shared/components/Block';
import { openNotification } from '../../../../../shared/utils/notificationsUtil';
import {
  WithCurrentUserProps,
  withExtractedCurrentUser
} from '../../../../../shared/containers/withExtractedCurrentUser';

interface State {
  submitting?: boolean;
  selectedRoles: PublicationRole[];
}

interface ParentProps {
  publicationDetail: PublicationDetail;
  selectedUser: User;
  userRoles: PublicationRole[];
  // jw: unlike the `canInviteRoles` this is filtered down to the roles that you can actually select for this user,
  //     with any roles the user already has filtered out.
  selectableRoles: PublicationRole[];
  onUserInvited: () => void;
}

interface Handlers {
  sendInvitation: () => void;
  changeRoleSelection: (role: PublicationRole, select: boolean) => void;
}

interface IsAddingSelfProps {
  isAddingSelf: boolean;
}

interface Props extends Handlers, IsAddingSelfProps {
  userRoleLookup: PublicationRoleLookupType;
  selectableRoleLookup: PublicationRoleLookupType;
  selectedRoleLookup: PublicationRoleLookupType;
  submitDisabled: boolean;
  submitting?: boolean;
}

const InvitePowerUserFormComponent: React.SFC<Props> = (props) => {
  const {
    sendInvitation,
    userRoleLookup,
    selectableRoleLookup,
    selectedRoleLookup,
    submitDisabled,
    submitting,
    changeRoleSelection,
    isAddingSelf
  } = props;

  return (
    <React.Fragment>
      <FormControl label={<FormattedMessage {...PublicationDetailsMessages.InviteRolesLabel}/>}>
        {EnhancedPublicationRole.enhancers.map(roleType => {
          const role = roleType.role;
          // jw: let's check the box if the user already has that role, or it is selected
          const selected = selectedRoleLookup[role] || userRoleLookup[role];
          const disabled = !selectableRoleLookup[role];

          return (
            <Checkbox
              key={`powerUserRole_${role}`}
              name="ignore"
              defaultChecked={selected}
              checked={selected}
              disabled={disabled}
              onClick={disabled ? undefined : () => changeRoleSelection(role, !selected)}
            >
              <FormattedMessage {...roleType.name}/>
            </Checkbox>
          );
        })}
      </FormControl>

      <Block style={{textAlign: 'center'}}>
        <Button
          size="large"
          type="primary"
          loading={submitting}
          onClick={sendInvitation}
          disabled={submitDisabled}
        >
          <FormattedMessage {...(isAddingSelf
            ? PublicationDetailsMessages.AddPowersButtonText
            : PublicationDetailsMessages.SendInvitationButtonText)}/>
        </Button>
      </Block>
    </React.Fragment>
  );
};

type HandlerProps = ParentProps &
  IsAddingSelfProps &
  InjectedIntlProps &
  WithStateProps<State> &
  WithInvitePublicationPowerUserProps;

export const InvitePowerUserForm = compose(
  withState<State>({selectedRoles: []}),
  withInvitePublicationPowerUser,
  injectIntl,
  withExtractedCurrentUser,
  // jw: to make life a little easier, let's isolate this flag into its own property resolution.
  withProps<IsAddingSelfProps, ParentProps & WithCurrentUserProps>((props: ParentProps & WithCurrentUserProps) => {
    const { currentUser, selectedUser } = props;

    const isAddingSelf = currentUser.oid === selectedUser.oid;

    return { isAddingSelf };
  }),
  withHandlers<HandlerProps, Handlers>({
    sendInvitation: (props) => async () => {
      const {
        setState,
        invitePublicationPowerUser,
        selectedUser,
        publicationDetail,
        onUserInvited,
        intl: { formatMessage } ,
        isAddingSelf
      } = props;

      setState(ss => ({...ss, submitting: true}));

      const roles = props.state.selectedRoles;
      const publicationOid = publicationDetail.oid;
      const userOid = selectedUser.oid;

      await handleFormlessServerOperation(() => invitePublicationPowerUser({roles}, publicationOid, userOid));

      const { displayName } = selectedUser;
      // Notify the user of success
      await openNotification.updateSuccess(
        {
          description: '',
          message: isAddingSelf
            ? formatMessage(PublicationDetailsMessages.PowersAddedConfirmation)
            : formatMessage(PublicationDetailsMessages.InvitationSentConfirmation, {displayName})
          ,
          duration: 5
        });

      onUserInvited();
    },
    changeRoleSelection: (props) => (role: PublicationRole, select: boolean) => {
      const { setState, state: { selectedRoles } } = props;

      setState(ss => ({...ss, selectedRoles: select
        // jw: if we are adding the role then concat it to the list
        ? selectedRoles.concat([role])
        // jw: if we are removing the role then remove it from the list
        : selectedRoles.filter(selectedRole => selectedRole !== role)
      }));
    }
  }),
  withProps<
    Omit<Props, 'sendInvitation' | 'changeRoleSelection' | 'isAddingSelf'>,
    ParentProps & WithStateProps<State>
  >((props: ParentProps & WithStateProps<State>) => {
    const { state: { selectedRoles, submitting }, userRoles, selectableRoles } = props;

    const userRoleLookup = getEnumLookupObject(userRoles) as PublicationRoleLookupType;
    const selectableRoleLookup = getEnumLookupObject(selectableRoles) as PublicationRoleLookupType;
    const selectedRoleLookup = getEnumLookupObject(selectedRoles) as PublicationRoleLookupType;
    const submitDisabled = selectedRoles.length === 0;

    return { userRoleLookup, selectableRoleLookup, selectedRoleLookup, submitDisabled, submitting };
  })
)(InvitePowerUserFormComponent) as React.ComponentClass<ParentProps>;
