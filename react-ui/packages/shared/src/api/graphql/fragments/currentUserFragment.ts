import gql from 'graphql-tag';
import { UserFragment } from './userFragment';
import { GlobalPermissionsFragment } from './globalPermissionsFragment';

export const CurrentUserFragment = gql`
  fragment CurrentUser on CurrentUser {
    user @type(name: "User") {
      ...User
    }
    personalJournalOid
    userAgeStatus
    formatPreferences @type(name: "FormatPreferences") {
      timeZone
      locale
      localeForNumber
    }
    globalPermissions @type(name: "GlobalPermissions") {
      ...GlobalPermissions
    }
  }
  ${UserFragment}
  ${GlobalPermissionsFragment}
`;
