import {
  PermissionType,
  withPermissionsModalController,
  WithPermissionsModalControllerProps
} from './withPermissionsModalController';
import { FormattedMessage } from 'react-intl';
import { branch, compose, renderComponent } from 'recompose';
import { ErrorCard } from '../components/card/ErrorCard';
import { PermissionMessages } from '../i18n/PermissionsMessages';
import * as React from 'react';

/*
  jw: The purpose of this HOC is to render the error card if the user is not granted the specified permission.
 */

export function withPermissionsCardInterceptor(
  permissionType: PermissionType,
  attemptedAction: FormattedMessage.MessageDescriptor,
  timeoutMessage?: FormattedMessage.MessageDescriptor
) {
  return compose(
    withPermissionsModalController(permissionType, attemptedAction, timeoutMessage),
    // jw: if the user does not have the permission granted, then render the permission error card.
    // bl: make sure to only do this once the current user has loaded to avoid showing it while loading.
    branch<WithPermissionsModalControllerProps>(props => !props.currentUserLoading && !props.granted,
      renderComponent<WithPermissionsModalControllerProps>((props: WithPermissionsModalControllerProps) => {
        const { permissionErrorModalProps } = props;

        if (!permissionErrorModalProps) {
          // todo:error-handling: How does the user not have the permission, and yet we do not have error modal props?
          return null;
        }

        const { description, extraInfo } = permissionErrorModalProps;
        const bodyProps = { description, extraInfo };

        return (
          <ErrorCard
            title={<FormattedMessage {...PermissionMessages.ErrorModalTitle}/>}
            gifType="newman"
            {...bodyProps}
          />
        );
      })
    )
  );
}
