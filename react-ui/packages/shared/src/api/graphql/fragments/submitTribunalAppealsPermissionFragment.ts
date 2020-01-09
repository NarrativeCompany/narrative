import gql from 'graphql-tag';
import { RevokablePermissionFragment } from './revokablePermissionFragment';

export const SubmitTribunalAppealsPermissionFragment = gql`
  fragment SubmitTribunalAppealsPermission on SubmitTribunalAppealsPermission {
    ...RevokablePermission
    revokeReason
  }
  ${RevokablePermissionFragment}
`;
