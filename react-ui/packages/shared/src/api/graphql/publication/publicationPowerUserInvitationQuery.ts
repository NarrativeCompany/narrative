import gql from 'graphql-tag';
import { PublicationPowerUserInvitationFragment } from '../fragments/publicationPowerUserInvitationFragment';

export const publicationPowerUserInvitationQuery = gql`
  query PublicationPowerUserInvitationQuery($publicationOid: String!) {
    getPublicationPowerUserInvitation (publicationOid: $publicationOid)
    @rest(
      type: "PublicationPowerUserInvitation", 
      path: "/publications/{args.publicationOid}/power-users/current-user/invitation"
    ) {
      ...PublicationPowerUserInvitation
    }
  }
  ${PublicationPowerUserInvitationFragment}
`;
