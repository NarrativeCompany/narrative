import gql from 'graphql-tag';
import { UserFollowersFragment } from '../fragments/userFollowersFragment';

export const userFollowersQuery = gql`
  query UserFollowersQuery ($input: FollowInput!, $filters: FollowFilterInput!) {
    getUserFollowers (input: $input, filters: $filters)
    @rest(
      type: "UserFollowers", 
      path: "/users/{args.input.userOid}/followers?{args.filters}"
    ) {
      ...UserFollowers
    }
  }
  ${UserFollowersFragment}
`;
