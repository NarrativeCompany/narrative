import gql from 'graphql-tag';
import { RevokablePermissionFragment } from './revokablePermissionFragment';

export const StandardPermissionFragment = gql`
  fragment StandardPermission on StandardPermission {
    ...RevokablePermission
    revokeReason
  }
  ${RevokablePermissionFragment}
`;
