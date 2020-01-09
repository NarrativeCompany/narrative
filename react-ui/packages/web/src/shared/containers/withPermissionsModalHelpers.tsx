import { FormattedMessage } from 'react-intl';
import { branch, compose, withProps } from 'recompose';
import {
  PermissionsModalControllerWithProps,
  PermissionType,
  withPermissionsModalController,
  WithPermissionsModalControllerProps
} from './withPermissionsModalController';
import { getSecuredLinkProps, LinkSecurerFunction, SecurableLinkProps } from '../components/Link';
import { withLoginModalHelpers, WithLoginModalHelpersProps } from './withLoginModalHelpers';

// jw: let's add some extra utility functions to the already useful withPermissionsModalController functionality.

export interface WithPermissionLinkSecurer {
  permissionLinkSecurer?: LinkSecurerFunction;
}

type Handlers =
  WithPermissionLinkSecurer;

export type WithPermissionsModalHelpersProps =
  PermissionsModalControllerWithProps &
  Handlers;

export function withPermissionsModalHelpers(
  permissionType: PermissionType,
  attemptedAction: FormattedMessage.MessageDescriptor,
  timeoutMessage?: FormattedMessage.MessageDescriptor
) {
  return compose(
    withLoginModalHelpers,
    branch((props: WithLoginModalHelpersProps) => !!props.loginLinkSecurer,
      // jw: if this is a guest, let's just defer to the login methods and not bother with security at all since we know
      //     they won't have it.
      withProps<Handlers, WithLoginModalHelpersProps>((props) => {
        const { loginLinkSecurer } = props;

        // jw: use this utility method to make sure that required attributes are present.
        return { permissionLinkSecurer: loginLinkSecurer };
      }),
      // jw: for a logged in user we need to check their permission and create a permissionLinkSecurer to show that
      compose(
        withPermissionsModalController(permissionType, attemptedAction, timeoutMessage),
        withProps<Handlers, WithPermissionsModalControllerProps>((props) => {
          const { granted, handleShowPermissionsModal } = props;

          // jw: if they have the right, no need to create a linkSecurer
          if (granted) {
            return {};
          }

          // jw: if they don't have the right then we want to secure the link and show the permission modal when clicked
          const permissionLinkSecurer = (linkProps: SecurableLinkProps) => {
            // jw: use this utility method to make sure that required attributes are present.
            return getSecuredLinkProps(linkProps, handleShowPermissionsModal);
          };

          return { permissionLinkSecurer };
        })
      )
    ),
  );
}
