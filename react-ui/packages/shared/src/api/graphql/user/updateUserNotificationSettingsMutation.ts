import gql from 'graphql-tag';
import { UserNotificationSettingsFragment } from '../fragments/userNotificationSettingsFragment';

export const updateUserNotificationSettingsMutation = gql`
  mutation UpdateUserNotificationSettingsMutation ($input: UserNotificationSettingsInput!) {
    updateUserNotificationSettings (input: $input) @rest(
      type: "UserNotificationSettings", 
      path: "/users/current/notification-settings",
      method: "PUT"
    ) {
      ...UserNotificationSettings
    }
  }
  ${UserNotificationSettingsFragment}
`;
