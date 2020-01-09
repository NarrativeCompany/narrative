import gql from 'graphql-tag';
import { AuthDetailFragment } from '../fragments/authDetailFragment';

export const loginUserMutation = gql`
  mutation LoginMutation ($input: LoginInput!) {
    login (input: $input) @rest(type: "AuthPayload", path: "/login", method: "POST") {
      ...AuthDetail
    }  
  }
  ${AuthDetailFragment}
`;
