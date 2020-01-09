import gql from 'graphql-tag';

export const UserPersonalSettingsFragment = gql`
  fragment UserPersonalSettings on UserPersonalSettings {
    qualityFilter
    displayAgeRestrictedContent
    hideMyFollowers
    hideMyFollows
  }
`;
