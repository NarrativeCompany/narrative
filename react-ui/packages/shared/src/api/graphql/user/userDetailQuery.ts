import gql from 'graphql-tag';
import { UserDetailFragment } from '../fragments/userDetailFragment';

export const userDetailQuery = gql`
  query UserDetailQuery ($userId: String!) {
    getUserDetail (userId: $userId) 
    @rest (type: "UserDetail", path: "/users/{args.userId}/detail") {
      ...UserDetail
    }
  }
  ${UserDetailFragment}
`;
