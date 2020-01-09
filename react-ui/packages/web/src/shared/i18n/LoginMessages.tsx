import { defineMessages } from 'react-intl';

export const LoginMessages = defineMessages({
  FormTitleModal: {
    id: 'login.formTitleModal',
    defaultMessage: 'Sign In'
  },
  FormTitleFullPage: {
    id: 'login.formTitleFullPage',
    defaultMessage: 'Welcome back!'
  },
  ConfirmFormTitle: {
    id: 'login.confirmFormTitle',
    defaultMessage: 'Confirm your credentials'
  },
  EmailInputPlaceholder: {
    id: 'login.emailInputPlaceholder',
    defaultMessage: 'email'
  },
  PasswordInputLabel: {
    id: 'login.passwordInputLabel',
    defaultMessage: 'Enter your password:'
  },
  PasswordInputPlaceholder: {
    id: 'login.passwordInputPlaceholder',
    defaultMessage: 'password'
  },
  RememberMe: {
    id: 'login.rememberMe',
    defaultMessage: 'Remember me'
  },
  ForgotPassword: {
    id: 'login.forgotPassword',
    defaultMessage: 'Forgot password'
  },
  SubmitBtnText: {
    id: 'login.submitBtnText',
    defaultMessage: 'Sign In'
  },
  RegisterLinkPrefix: {
    id: 'login.registerLinkPrefix',
    defaultMessage: 'Not a member?'
  },
  ConfirmSubmitBtnText: {
    id: 'login.confirmSubmitBtnText',
    defaultMessage: 'Submit'
  },
  RegisterLinkText: {
    id: 'login.registerLinkText',
    defaultMessage: 'Register now!'
  },
  ReturnHomeLinkText: {
    id: 'login.returnToHome',
    defaultMessage: 'Take me to Narrative'
  },
  SignOutLinkText: {
    id: 'twoFactorLogin.signOutLinkText',
    defaultMessage: 'Sign Out'
  },
  LoginExpired: {
    id: 'login.loginExpired',
    defaultMessage: 'Your session has expired.  Please sign in to Narrative.'
  },
  LoginRequired: {
    id: 'login.loginRequired',
    defaultMessage: 'You must sign in to continue.'
  },
  EmailVerifiedLoginRequired: {
    id: 'login.emailVerifiedLoginRequired',
    defaultMessage: 'Your email has been successfully confirmed! Please sign in to continue.'
  },
  TooManyRequests: {
    id: 'login.tooManyRequests',
    defaultMessage: '• You have exceeded the rate limit for login requests. For security, you must wait 15 minutes ' +
      'before trying another login request.'
  }
});
