import { defineMessages } from 'react-intl';

export const MemberSecuritySettingsMessages = defineMessages({
  SEOTitle: {
    id: 'memberSecuritySettings.seoTitle',
    defaultMessage: 'Member CP - Security Settings'
  },
  SectionSecurity: {
    id: 'memberSecuritySettings.sectionSecurity',
    defaultMessage: 'Two-Factor Authentication'
  },
  TwoFactorAuthMessage: {
    id: 'memberSecuritySettings.twoFactorAuthMessage',
    defaultMessage: 'To further secure your account, you can set up Two-Factor Authentication.  Two-Factor' +
      ' Authentication requires that you have your mobile device in your possession in order to sign in. This is' +
      ' accomplished by registering your account on the Google Authenticator App, which provides security codes' +
      ' for you to provide when you authenticate with Narrative. The Google Authenticator App is available for' +
      ' {iosLink} and {androidLink}.'
  },
  TwoFactorAuthControlLabel: {
    id: 'memberSecuritySettings.twoFactorAuthControlLabel',
    defaultMessage: 'Two-Factor Auth'
  }
});
