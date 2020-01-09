import { defineMessages } from 'react-intl';

export const Enable2FAMessages = defineMessages({
  PageTitle: {
    id: 'enable2FAMessages.pageTitle',
    defaultMessage: 'Enable Two-Factor Authentication'
  },
  StepOneTitle: {
    id: 'enable2FAMessages.stepOneTitle',
    defaultMessage: 'Step 1. '
  },
  StepOne: {
    id: 'enable2FAMessages.stepOnePartOne',
    defaultMessage: 'Open the Google Authenticator App (available on {iosLink} and {androidLink}) and scan the' +
      ' QR code below.'
  },
  StepOneIOSLinkText: {
    id: 'enable2FAMessages.stepOneIOSLinkText',
    defaultMessage: 'iOS'
  },
  StepOneAndroidLink: {
    id: 'enable2FAMessages.stepOneAndroidLinkText',
    defaultMessage: 'Android'
  },
  ShowSecretKey: {
    id: 'enable2FAMessages.showSecretKey',
    defaultMessage: 'Show Secret Key'
  },
  StepTwoTitle: {
    id: 'enable2FAMessages.stepTwoTitle',
    defaultMessage: 'Step 2. '
  },
  StepTwoMessage: {
    id: 'enable2FAMessages.stepTwoMessage',
    defaultMessage: 'Enter your password and the 6-digit code to confirm you have successfully registered your device.'
  },
  BackupCodes: {
    id: 'enable2FAMessages.backupCodes',
    defaultMessage: 'Backup Codes'
  },
  Print: {
    id: 'enable2FAModalPartTwo.print',
    defaultMessage: 'Print'
  },
  Close: {
    id: 'enable2FAModalPartTwo.close',
    defaultMessage: 'Close'
  },
  StepThreeTitle: {
    id: 'enable2FAModalPartTwo.stepThreeTitle',
    defaultMessage: 'Step 3. Important! '
  },
  StepThreeMessage: {
    id: 'enable2FAModalPartTwo.stepThreeMessage',
    defaultMessage: 'Print or store these backup codes in a safe place. Without these codes, you will not be able' +
      ' to recover your account if you lose access to your Google Authenticator app. You will not be able to' +
      ' re-generate these backup codes again.'
  },
});
