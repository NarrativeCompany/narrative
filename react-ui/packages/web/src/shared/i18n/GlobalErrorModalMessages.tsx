import { defineMessages } from 'react-intl';

export const GlobalErrorModalMessages = defineMessages({
  UnhandledErrorLabel: {
    id: 'globalErrorModalMessages.undhandledErrorLabel',
    defaultMessage: 'An unexpected error occurred'
  },
  UnhandledErrorDismissLabel: {
    id: 'globalErrorModalMessages.unhandledErrorDismissLabel',
    defaultMessage: 'Dismiss'
  },
  ErrorTypeUnknownError: {
    id: 'globalErrorModalMessages.errorTypeUnknownError',
    defaultMessage: 'An unknown error was encountered.  Please try your request again later.  {message}'
  },
  ErrorTypeNotFound: {
    id: 'globalErrorModalMessages.errorTypeNotFound',
    defaultMessage: 'The resource you are attempting to access was not found.  {message}'
  },
  ErrorTypeLoginRequired: {
    id: 'globalErrorModalMessages.errorTypeLoginRequired',
    defaultMessage: 'The resource you are attempting to access requires that you log into your account.  {message}'
  },
  ErrorTypeAccessDenied: {
    id: 'globalErrorModalMessages.errorTypeAccessDenied',
    defaultMessage: 'You do not have permission to access the requested resource.  {message}'
  },
  AccessDeniedTitle: {
    id: 'globalErrorModalMessages.accessDeniedTitle',
    defaultMessage: 'Access Denied'
  },
  ActivityRateExceededTitle: {
    id: 'globalErrorModalMessages.activityRateLimitedTitle',
    defaultMessage: 'Error'
  },
  ErrorTypeTOSRequired: {
    id: 'globalErrorModalMessages.errorTypeTOSAgreementRequired',
    defaultMessage: 'You must accepted the Terms Of Service Agreement in order to access the requested ' +
      'resource.  {message}'
  },
  ErrorTypeEmailVerificationRequired: {
    id: 'globalErrorModalMessages.errorTypeEmailVerificationRequired',
    defaultMessage: 'You must activate your account via the provided link in your welcome email in order to access ' +
      'the requested resource.  {message}'
  },
  ErrorTypeServerUnreachable: {
    id: 'globalErrorModalMessages.errorTypeServerUnreachable',
    defaultMessage: 'The server is currently unreachable.  Please try your request again later.  {message}'
  },
  Error: {
    id: 'globalErrorModalMessages.error',
    defaultMessage: 'Error'
  }
});
