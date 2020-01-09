import gql from 'graphql-tag';
import { PermissionFragment } from './permissionFragment';

export const RevokablePermissionFragment = gql`
  fragment RevokablePermission on RevokablePermission {
    ...Permission
    restorationDatetime
  }
  ${PermissionFragment}
`;
