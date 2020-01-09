import { defineMessages } from 'react-intl';

export const TwoFactorLoginMessages = defineMessages({
  FormTitle: {
    id: 'twoFactor.formTitle',
    defaultMessage: 'Two-Factor Authentication'
  },
  VerificationCodeInputLabel: {
    id: 'twoFactor.verificationCodeInputLabel',
    defaultMessage: 'Enter the 6-digit code from the Google Authenticator App'
  },
  VerificationCodeInputPlaceholder: {
    id: 'twoFactor.verificationCodeInputPlaceholder',
    defaultMessage: '6-digit code'
  },
  RememberMe: {
    id: 'twoFactor.rememberMe',
    defaultMessage: 'Remember for 30 Days'
  },
  SubmitBtnText: {
    id: 'twoFactor.submitBtnText',
    defaultMessage: 'Submit'
  },
  TwoFactorExpired: {
    id: 'twoFactor.twoFactorExpired',
    defaultMessage: 'Your two-factor login session has expired. Please enter a new two-factor code to sign ' +
      'in to Narrative.'
  }
});
