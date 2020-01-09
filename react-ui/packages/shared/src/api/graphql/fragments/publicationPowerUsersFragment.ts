import gql from 'graphql-tag';
import { UserFragment } from './userFragment';
import { PublicationDetailFragment } from './publicationDetailFragment';

export const PublicationPowerUsersFragment = gql`
  fragment PublicationPowerUsers on PublicationPowerUsers {
    oid
    
    currentUserCanManageRoles
    currentUserAllowedInviteRoles
  
    editorLimit
    writerLimit

    admins @type(name: "User") {
      ...User
    }
    editors @type(name: "User") {
      ...User
    }
    writers @type(name: "User") {
      ...User
    }

    invitedAdmins @type(name: "User") {
      ...User
    }
    invitedEditors @type(name: "User") {
      ...User
    }
    invitedWriters @type(name: "User") {
      ...User
    }

    publicationDetail @type(name: "PublicationDetail") {
      ...PublicationDetail
    }
  }
  ${UserFragment}
  ${PublicationDetailFragment}
`;
