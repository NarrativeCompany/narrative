import gql from 'graphql-tag';
import { CurrentUserFragment } from '../fragments/currentUserFragment';

export const currentUserQuery = gql`
  query CurrentUserQuery {
    getCurrentUser @rest(type: "CurrentUser", path: "/users/current") {
      ...CurrentUser
    }
  }
  ${CurrentUserFragment}
`;
