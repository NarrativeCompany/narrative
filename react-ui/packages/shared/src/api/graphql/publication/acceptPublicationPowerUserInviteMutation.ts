import gql from 'graphql-tag';

export const acceptPublicationPowerUserInviteMutation = gql`
  mutation AcceptPublicationPowerUserInviteMutation ($input: PublicationInvitationResponseInput!) {
    acceptPublicationPowerUserInvite (input: $input) @rest(
      type: "VoidResult", 
      path: "/publications/{args.input.publicationOid}/power-users/current-user/invitation", 
      method: "PUT"
    ) {
      success
    }
  }
`;
