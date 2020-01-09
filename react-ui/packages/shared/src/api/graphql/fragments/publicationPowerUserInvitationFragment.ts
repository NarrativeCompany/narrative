import gql from 'graphql-tag';

export const PublicationPowerUserInvitationFragment = gql`
  fragment PublicationPowerUserInvitation on PublicationPowerUserInvitation {
    oid

    invitedRoles
  }
`;
