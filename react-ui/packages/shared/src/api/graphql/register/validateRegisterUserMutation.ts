import gql from 'graphql-tag';

export const validateRegisterUserMutation = gql`
  mutation ValidateRegisterUserMutation ($input: RegisterUserInput!) {
    validateRegisterUser (input: $input)
    @rest(type: "StringScalar", path: "/users/validate-registering-user" method: "POST") {
      recaptchaToken: value
    }
  }
`;
