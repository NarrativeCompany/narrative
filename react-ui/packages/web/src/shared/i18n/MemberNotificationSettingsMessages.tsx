import { defineMessages } from 'react-intl';

export const MemberNotificationSettingsMessages = defineMessages({
  SEOTitle: {
    id: 'memberNotificationSettings.seoTitle',
    defaultMessage: 'Member CP - Notification Settings'
  },
  NotifyWhenFollowed: {
    id: 'memberNotificationSettingsForm.notifyWhenFollowed',
    defaultMessage: 'Someone follows me'
  },
  NotifyWhenMentioned: {
    id: 'memberNotificationSettingsForm.notifyWhenMentioned',
    defaultMessage: 'Someone mentions me'
  },
  SuspendAllEmails: {
    id: 'memberNotificationSettingsForm.suspendAllEmails',
    defaultMessage: 'Suspend all notifications'
  },
  InfluenceNotifications: {
    id: 'memberNotificationSettingsForm.influenceNotifications',
    defaultMessage: 'Email me when'
  },
  Miscellaneous: {
    id: 'memberNotificationSettingsForm.miscellaneous',
    defaultMessage: 'Or'
  },
  UpdateSettings: {
    id: 'memberNotificationSettingsForm.updateSettings',
    defaultMessage: 'Update Settings'
  },
  NotificationSettingsUpdated: {
    id: 'memberNotificationSettingsForm.notificationSettingsUpdated',
    defaultMessage: 'Your notification settings have been saved.'
  },
});
