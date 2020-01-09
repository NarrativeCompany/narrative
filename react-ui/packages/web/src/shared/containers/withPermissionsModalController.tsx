import { compose, withHandlers, withProps } from 'recompose';
import {
  GlobalPermissions,
  RevokablePermission,
  withState,
  WithStateProps
} from '@narrative/shared';
import { FormattedMessage } from 'react-intl';
import { withExtractedCurrentUser, WithExtractedCurrentUserProps } from './withExtractedCurrentUser';
import {
  getRevokeReasonProps,
  RevokeReasonType,
  RevokeReasonProps
} from '../utils/revokeReasonMessagesUtil';
import { PermissionErrorModalProps } from '../components/PermissionErrorModal';

export type PermissionType = keyof GlobalPermissions;

export interface PermissionProps extends RevokablePermission {
  revokeReason: RevokeReasonType | null;
}

export interface PermissionsModalControllerWithProps {
  granted: boolean;
  permissionErrorModalProps?: PermissionErrorModalProps;
}

interface PermissionsModalControllerState {
  isPermissionsModalVisible: boolean;
}
const initialPermissionsCheckControllerState: PermissionsModalControllerState = {
  isPermissionsModalVisible: false
};

interface PermissionsModalControllerHandlers {
  handleShowPermissionsModal: () => void;
  handleDismissPermissionsModal: () => void;
}

export function isPermissionGranted(permissionType: PermissionType, permissions?: GlobalPermissions): boolean {
  if (!permissions) {
    return false;
  }

  const permission = permissions[permissionType] as PermissionProps;

  if (!permission) {
   return false;
  }

  return permission.granted;
}

export type WithPermissionsModalControllerProps =
  WithExtractedCurrentUserProps &
  PermissionsModalControllerWithProps &
  WithStateProps<PermissionsModalControllerState> &
  PermissionsModalControllerHandlers;

export function withPermissionsModalController(
  permissionType: PermissionType,
  attemptedAction: FormattedMessage.MessageDescriptor,
  timeoutMessage?: FormattedMessage.MessageDescriptor
) {
  return compose(
    withState<PermissionsModalControllerState>(initialPermissionsCheckControllerState),
    withExtractedCurrentUser,
    withProps((props: WithStateProps<PermissionsModalControllerState> & WithExtractedCurrentUserProps) => {
      // jw: this is the most questionable thing this code does. We know that permissionType is a key of
      //     GlobalPermissions, so I think it's safe to assume that the provided object will fulfill the PermissionProps
      //     By doing this, we are mitigating the amount of boilerplate necessary when adding permissions.
      const permission =
        props.currentUserGlobalPermissions &&
        props.currentUserGlobalPermissions[permissionType] as PermissionProps;
      const currentUser = props.currentUser;

      const { state, setState } = props;

      const revokeReasonProps: RevokeReasonProps = getRevokeReasonProps(
        attemptedAction,
        permission,
        currentUser,
        timeoutMessage,
        () => setState(ss => ({ ...ss, isPermissionsModalVisible: false}))
      );

      const { granted, errorMessage, restorationMessage } = revokeReasonProps;

      // jw: we only want to setup the props if the permission is not granted
      let permissionErrorModalProps: PermissionErrorModalProps | undefined;
      if (!granted) {
        permissionErrorModalProps = {
          extraInfo: restorationMessage,
          visible: state.isPermissionsModalVisible,
          description: errorMessage,
          dismiss: () => setState(ss => ({ ...ss, isPermissionsModalVisible: false }))
        };
      }

      // jw: finally, we are ready to return these results.
      return { granted, errorMessage, permissionErrorModalProps };
    }),
    withHandlers<PermissionsModalControllerWithProps & WithStateProps<PermissionsModalControllerState>, {}>({
      handleShowPermissionsModal: (props) => () => {
        const { setState, granted } = props;

        if (!granted) {
          setState(ss => ({ ...ss, isPermissionsModalVisible: true }));
        }
      },
      handleDismissPermissionsModal: (props) => () =>
        props.setState(ss => ({ ...ss, isPermissionsModalVisible: false }))
    })
  );
}
