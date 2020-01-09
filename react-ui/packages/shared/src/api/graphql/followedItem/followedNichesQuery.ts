import gql from 'graphql-tag';
import { FollowedNichesFragment } from '../fragments/followedNichesFragment';

export const followedNichesQuery = gql`
  query FollowedNichesQuery ($input: FollowInput!, $filters: FollowFilterInput!) {
    getFollowedNiches (input: $input, filters: $filters)
    @rest(
      type: "FollowedNiches", 
      path: "/users/{args.input.userOid}/follows/niches?{args.filters}"
    ) {
      ...FollowedNiches
    }
  }
  ${FollowedNichesFragment}
`;
