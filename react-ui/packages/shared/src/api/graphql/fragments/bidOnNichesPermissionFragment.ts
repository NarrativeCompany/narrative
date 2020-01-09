import gql from 'graphql-tag';
import { RevokablePermissionFragment } from './revokablePermissionFragment';

export const BidOnNichesPermissionFragment = gql`
  fragment BidOnNichesPermission on BidOnNichesPermission {
    ...RevokablePermission
    revokeReason
  }
  ${RevokablePermissionFragment}
`;
