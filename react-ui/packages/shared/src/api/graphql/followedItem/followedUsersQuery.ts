import gql from 'graphql-tag';
import { FollowedUsersFragment } from '../fragments/followedUsersFragment';

export const followedUsersQuery = gql`
  query FollowedUsersQuery ($input: FollowInput!, $filters: FollowFilterInput!) {
    getFollowedUsers (input: $input, filters: $filters)
    @rest(
      type: "FollowedUsers", 
      path: "/users/{args.input.userOid}/follows/users?{args.filters}"
    ) {
      ...FollowedUsers
    }
  }
  ${FollowedUsersFragment}
`;
