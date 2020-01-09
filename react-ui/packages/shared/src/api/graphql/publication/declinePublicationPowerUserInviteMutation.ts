import gql from 'graphql-tag';

export const declinePublicationPowerUserInviteMutation = gql`
  mutation DeclinePublicationPowerUserInviteMutation ($input: PublicationInvitationResponseInput!) {
    declinePublicationPowerUserInvite (input: $input) @rest(
      type: "VoidResult", 
      path: "/publications/{args.input.publicationOid}/power-users/current-user/invitation", 
      method: "DELETE"
    ) {
      success
    }
  }
`;
