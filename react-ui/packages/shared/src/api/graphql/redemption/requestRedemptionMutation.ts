import gql from 'graphql-tag';

export const requestRedemptionMutation = gql`
  mutation RequestRedemptionMutation ($input: RequestRedemptionInput!) {
    requestRedemption (input: $input) @rest(
      type: "VoidResult", 
      path: "/users/current/neo-wallet/redemptions", 
      method: "POST"
    ) {
      success
    }
  }
`;
