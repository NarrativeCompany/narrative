import { defineMessages } from 'react-intl';

export const MemberAccountSettingsMessages = defineMessages({
  SEOTitle: {
    id: 'memberAccountSettings.seoTitle',
    defaultMessage: 'Member CP - Account Settings'
  },
  SectionCredentials: {
    id: 'memberAccountSettings.sectionCredentials',
    defaultMessage: 'Credentials'
  },
  SectionCredentialsDescription: {
    id: 'memberAccountSettings.sectionCredentialsDescription',
    defaultMessage: 'Your account information is always private.'
  },
  DeleteAccountLabel: {
    id: 'memberAccountSettings.deleteAccountLabel',
    defaultMessage: 'Delete Account'
  },
  EmailAddressLabel: {
    id: 'memberAccountSettings.emailAddressLabel',
    defaultMessage: 'Email Address'
  },
  UpdateEmailAddressLabel: {
    id: 'memberAccountSettings.updateEmailAddressLabel',
    defaultMessage: 'Update email address'
  },
  PasswordLabel: {
    id: 'memberAccountSettings.passwordLabel',
    defaultMessage: 'Password'
  },
  UpdatePasswordLabel: {
    id: 'memberAccountSettings.updatePasswordLabel',
    defaultMessage: 'Change password'
  },
  SectionTermsOfService: {
    id: 'memberAccountSettings.sectionTermsOfService',
    defaultMessage: 'Legal Stuff'
  },
  TermsOfServiceDescription: {
    id: 'memberAccountSettings.TermsOfServiceDescription',
    defaultMessage: 'You agreed to the {tosLink}.'
  },
  RevokeAgreementLabel: {
    id: 'memberAccountSettings.revokeAgreementLabel',
    defaultMessage: 'Revoke Agreement'
  },
  NewEmailAddressLabel: {
    id: 'memberAccountSettings.newEmailAddressLabel',
    defaultMessage: 'Email address'
  },
  ConfirmNewEmailAddressLabel: {
    id: 'memberAccountSettings.confirmNewEmailAddressLabel',
    defaultMessage: 'Confirm email address'
  },
  TOSAcceptTitle: {
    id: 'globalErrorModalMessages.tosAcceptTitle',
    defaultMessage: 'Accept Terms of Service'
  },
  TOSAcceptDescription: {
    id: 'globalErrorModalMessages.tosAcceptDescription',
    defaultMessage: 'In order to continue as a Narrative member, you must agree to the {tosLink}.  Do you agree to ' +
      'the Narrative Terms of Service?'
  },
  TOSAcceptSubmitLabel: {
    id: 'globalErrorModalMessages.tosAcceptSubmitLabel',
    defaultMessage: 'I Agree'
  },
  AcceptTOSSuccessful: {
    id: 'memberAccountSettings.acceptTOSSuccessful',
    defaultMessage: 'Terms of Service agreement accepted!  Welcome back to Narrative!'
  },
  EmailConfirmationRequiredTitle: {
    id: 'memberAccountSettings.emailConfirmationRequiredTitle',
    defaultMessage: 'Email Confirmation Required'
  },
  EmailConfirmationRequiredMessage: {
    id: 'memberAccountSettings.emailConfirmationRequiredMessagePart',
    defaultMessage: 'Your email address must be confirmed. Click {link} to have the confirmation email re-sent.'
  },
  EmailConfirmationRequiredLinkLabel: {
    id: 'memberAccountSettings.emailConfirmationRequiredLinkLabel',
    defaultMessage: 'resend'
  },
  EmailConfirmationResentMessage: {
    id: 'memberAccountSettings.emailConfirmationResentMessage',
    defaultMessage: 'A confirmation email has been sent to the email address you provided during registration.  ' +
      'Please check your inbox and confirm your email address via the link provided in the confirmation email.'
  },
  YouHaveUntilXToVerifyOneStep: {
    id: 'PendingEmailAddressField.youHaveUntilXToVerifyOneStep',
    defaultMessage: 'You have until {expirationDatetime} to click the verification link sent to {otherEmailAddress}.'
  },
  YouHaveUntilXToVerifyTwoSteps: {
    id: 'PendingEmailAddressField.youHaveUntilXToVerifyTwoSteps',
    defaultMessage: 'You have until {expirationDatetime} to click the verification links sent to' +
      ' {emailAddress} and {pendingEmailAddress}.'
  },
  PendingEmailAddress: {
    id: 'pendingEmailAddressField.pendingEmailAddress',
    defaultMessage: 'Pending Email Address'
  },
});
