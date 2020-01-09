import gql from 'graphql-tag';

export const cancelRedemptionRequestMutation = gql`
  mutation CancelRedemptionRequestMutation ($input: CancelRedemptionRequestInput!) {
    cancelRedemptionRequest (input: $input) @rest(
      type: "VoidResult", 
      path: "/users/current/neo-wallet/redemptions/{args.input.redemptionOid}", 
      method: "DELETE"
    ) {
      success
    }
  }
`;
