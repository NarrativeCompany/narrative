import gql from 'graphql-tag';
import { RevokablePermissionFragment } from './revokablePermissionFragment';

export const SuggestNichesPermissionFragment = gql`
  fragment SuggestNichesPermission on SuggestNichesPermission {
    ...RevokablePermission
    revokeReason
  }
  ${RevokablePermissionFragment}
`;
