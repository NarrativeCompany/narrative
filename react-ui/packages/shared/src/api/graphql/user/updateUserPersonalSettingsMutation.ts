import gql from 'graphql-tag';
import { UserPersonalSettingsFragment } from '../fragments/userPersonalSettingsFragment';

export const updateUserPersonalSettingsMutation = gql`
  mutation UpdateUserPersonalSettingsMutation ($input: UserPersonalSettingsInput!) {
    updateUserPersonalSettings (input: $input) @rest(
      type: "UserPersonalSettings", 
      path: "/users/current/personal-settings",
      method: "PUT"
    ) {
      ...UserPersonalSettings
    }
  }
  ${UserPersonalSettingsFragment}
`;
