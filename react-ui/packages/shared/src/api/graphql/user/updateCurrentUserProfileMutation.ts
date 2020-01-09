import gql from 'graphql-tag';
import { UserFragment } from '../fragments/userFragment';

export const updateCurrentUserProfileMutation = gql`
  mutation UpdateCurrentUserProfileMutation ($input: UpdateCurrentUserProfileInput!) {
    updateCurrentUserProfile (input: $input) @rest(
      type: "User", 
      path: "/users/current",
      method: "PUT"
    ) {
      ...User
    }
  }
  ${UserFragment}
`;
