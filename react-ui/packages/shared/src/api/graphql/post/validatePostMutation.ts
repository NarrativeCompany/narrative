import gql from 'graphql-tag';

export const validatePostMutation = gql`
  mutation ValidatePostMutation ($input: PostTextInput!) {
    validatePostText (input: $input) @rest(type: "VoidResult", path: "/posts/validate-text" method: "POST") {
      success
    }
  }
`;
