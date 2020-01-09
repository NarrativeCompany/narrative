import { defineMessages } from 'react-intl';

export const RecoverPasswordMessages = defineMessages({
  FormTitle: {
    id: 'recoverPasswordMessages.formTitle',
    defaultMessage: 'Reset Your Password'
  },
  Description: {
    id: 'recoverPasswordMessages.description',
    defaultMessage: 'Provide your account email address below and we will email you a special link so that you can ' +
      'reset your password.'
  },
  EmailInputLabel: {
    id: 'recoverPasswordMessages.passwordLabel',
    defaultMessage: 'Your Narrative Email Address'
  },
  SubmitLabel: {
    id: 'recoverPasswordMessages.resetPassword',
    defaultMessage: 'Reset Password'
  },
  SuccessMessage: {
    id: 'recoverPasswordMessages.successMessage',
    defaultMessage: 'We have sent you an email to allow you to reset the password on your account. Please check your ' +
      'email now. The link in the email is only valid for one hour, so if you don\'t use the link soon, your account ' +
      'will remain unchanged.'
  }
});
