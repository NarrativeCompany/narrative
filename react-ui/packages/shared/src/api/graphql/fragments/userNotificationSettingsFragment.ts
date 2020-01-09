import gql from 'graphql-tag';

export const UserNotificationSettingsFragment = gql`
  fragment UserNotificationSettings on UserNotificationSettings {
    notifyWhenFollowed
    notifyWhenMentioned
    suspendAllEmails
  }
`;
