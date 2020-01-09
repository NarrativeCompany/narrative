import gql from 'graphql-tag';
import { UserNotificationSettingsFragment } from '../fragments/userNotificationSettingsFragment';

export const userNotificationSettingsQuery = gql`
  query UserNotificationSettingsQuery {
    getUserNotificationSettings @rest(type: "UserNotificationSettings", path: "/users/current/notification-settings") {
      ...UserNotificationSettings
    }
  }
  ${UserNotificationSettingsFragment}
`;
