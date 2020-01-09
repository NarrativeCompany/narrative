import * as React from 'react';
import { ErrorModal, ErrorModalParentProps } from './ErrorModal';
import { FormattedMessage } from 'react-intl';
import { PermissionMessages } from '../i18n/PermissionsMessages';

export type PermissionErrorModalProps =
  Pick<ErrorModalParentProps, 'visible' | 'dismiss' | 'description' | 'extraInfo'>;

export const PermissionErrorModal: React.SFC<PermissionErrorModalProps> = (props) => {
  return (
    <ErrorModal
      {...props}
      title={<FormattedMessage {...PermissionMessages.ErrorModalTitle}/>}
      gifType="newman"
      btnText={<FormattedMessage {...PermissionMessages.ErrorModalBtnText}/>}
    />
  );
};
