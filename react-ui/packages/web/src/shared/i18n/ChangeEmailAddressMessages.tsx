import { defineMessages } from 'react-intl';

export const ChangeEmailAddressMessages = defineMessages({
  PageTitle: {
    id: 'changeEmailAddress.pageTitle',
    defaultMessage: 'Update Email Address'
  },
  NewEmailAddressLabel: {
    id: 'changeEmailAddress.newEmailAddressLabel',
    defaultMessage: 'New email address'
  },
  ConfirmNewEmailAddressLabel: {
    id: 'changeEmailAddress.confirmNewEmailAddressLabel',
    defaultMessage: 'Confirm new email address'
  },
  EmailChangeRequested: {
    id: 'changeEmailAddress.emailChangeRequested',
    defaultMessage: 'Email Change Submitted'
  },
  EmailChangeRequestedMessage: {
    id: 'changeEmailAddress.emailChangeRequestedMessage',
    defaultMessage: 'We have sent an email to your current email address ({currentEmailAddress}) and your new' +
      ' email address ({newEmailAddress}) to verify the change. You have 24 hours to click the verification' +
      ' links in both emails to complete the process or else the change will be canceled automatically.'
  },
  EmailAddressVerificationCompleteTitle: {
    id: 'confirmEmailChange.emailAddressVerificationCompleteTitle',
    defaultMessage: 'Email Verified'
  },
  EmailAddressVerificationCompleteMessage: {
    id: 'confirmEmailChange.emailAddressVerificationCompleteMessage',
    defaultMessage: 'Your email address has been successfully changed to {emailAddress}.'
  },
  EmailAddressVerifiedTitle: {
    id: 'confirmEmailChange.emailAddressVerifiedTitle',
    defaultMessage: 'Almost There!'
  },
  EmailAddressVerifiedMessage: {
    id: 'confirmEmailChange.emailAddressVerifiedMessage',
    defaultMessage: 'Before your new email address will become active, you must also click the verification link' +
      ' sent to {emailAddressToVerify}.'
  },
  PendingEmailAddressVerifiedMessage: {
    id: 'confirmEmailChange.pendingEmailAddressVerifiedMessage',
    defaultMessage: 'Before your new email address will become active, you must also click the verification link' +
      ' sent to your current email address.'
  },
  EmailAddressChangeCanceledTitle: {
    id: 'cancelEmailChange.emailAddressChangeCanceledTitle',
    defaultMessage: 'Email Change Canceled'
  },
  EmailAddressChangeCanceledMessage: {
    id: 'cancelEmailChange.emailAddressChangeCanceledMessage',
    defaultMessage: 'The request to change the email address to {emailAddress} has been canceled.'
  },
});
