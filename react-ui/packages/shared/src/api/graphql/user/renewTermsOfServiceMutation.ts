import gql from 'graphql-tag';

export const renewTermsOfServiceMutation = gql`
  mutation RenewTermsOfServiceMutation ($input: UserRenewTosAgreementInput!) {
    renewTermsOfService (input: $input) 
    @rest(
      type: "VoidResult", 
      path: "/users/current/tos-agreement", 
      method: "PUT"
    ) {
      success
    }
  }
`;
