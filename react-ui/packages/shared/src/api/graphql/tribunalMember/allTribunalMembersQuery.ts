import gql from 'graphql-tag';
import { UserFragment } from '../fragments/userFragment';

export const allTribunalMembersQuery = gql`
  query AllTribunalMembersQuery {
    getAllTribunalMembers @rest(type: "User", path: "/tribunal/members") {
      ...User
    }
  }
  ${UserFragment}
`;
