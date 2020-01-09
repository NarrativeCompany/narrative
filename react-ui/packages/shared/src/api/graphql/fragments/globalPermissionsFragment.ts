import gql from 'graphql-tag';
import { SuggestNichesPermissionFragment } from './suggestNichesPermissionFragment';
import { BidOnNichesPermissionFragment } from './bidOnNichesPermissionFragment';
import { SubmitTribunalAppealsPermissionFragment } from './submitTribunalAppealsPermissionFragment';
import { PermissionFragment } from './permissionFragment';
import { StandardPermissionFragment } from './standardPermissionFragment';

export const GlobalPermissionsFragment = gql`
  fragment GlobalPermissions on GlobalPermissions {
    suggestNiches @type(name: "SuggestNichesPermission") {
      ...SuggestNichesPermission
    }
    bidOnNiches @type(name: "BidOnNichesPermission") {
      ...BidOnNichesPermission
    }
    createPublications @type(name: "StandardPermission"){
      ...StandardPermission
    }
    participateInTribunalActions @type(name: "Permission") {
      ...Permission
    }
    removeAupViolations @type(name: "Permission") {
      ...Permission
    }
    submitTribunalAppeals @type(name: "SubmitTribunalAppealsPermission"){
      ...SubmitTribunalAppealsPermission
    }
    voteOnApprovals @type(name: "StandardPermission"){
      ...StandardPermission
    }
    rateContent @type(name: "StandardPermission"){
      ...StandardPermission
    }
    nominateForModeratorElection @type(name: "StandardPermission"){
      ...StandardPermission
    }
    postContent @type(name: "StandardPermission"){
      ...StandardPermission
    }
    postComments @type(name: "StandardPermission"){
      ...StandardPermission
    }
  }
  ${SuggestNichesPermissionFragment}
  ${BidOnNichesPermissionFragment}
  ${SubmitTribunalAppealsPermissionFragment}
  ${PermissionFragment}
  ${StandardPermissionFragment}
`;
