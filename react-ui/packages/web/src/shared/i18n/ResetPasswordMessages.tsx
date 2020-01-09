import { defineMessages } from 'react-intl';

export const ResetPasswordMessages = defineMessages({
  PageTitle: {
    id: 'resetPassword.pageTitle',
    defaultMessage: 'Reset Password'
  },
  NewPasswordLabel: {
    id: 'resetPassword.newPasswordLabel',
    defaultMessage: 'New password'
  },
  ConfirmNewPasswordLabel: {
    id: 'resetPassword.confirmNewPasswordLabel',
    defaultMessage: 'Confirm new password'
  },
  SuccessMessage: {
    id: 'resetPassword.successMessage',
    defaultMessage: 'Your password has been successfully reset, so you should be able to sign in now!'
  },
  BadURLMessage: {
    id: 'resetPassword.badURLMessage',
    defaultMessage: 'This account recovery URL is invalid. You will have to request a new email to recover your ' +
      'account.'
  },
  ExpiredURLMessage: {
    id: 'resetPassword.expiredUrlMessage',
    defaultMessage: 'This account recovery URL has expired. You will have to request a new email to recover your ' +
      'account.'
  }
});
