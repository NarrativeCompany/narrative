import gql from 'graphql-tag';
import { UserPersonalSettingsFragment } from '../fragments/userPersonalSettingsFragment';

export const userPersonalSettingsQuery = gql`
  query UserPersonalSettingsQuery {
    getUserPersonalSettings @rest(type: "UserPersonalSettings", path: "/users/current/personal-settings") {
      ...UserPersonalSettings
    }
  }
  ${UserPersonalSettingsFragment}
`;
