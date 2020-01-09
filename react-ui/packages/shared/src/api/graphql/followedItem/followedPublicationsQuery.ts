import gql from 'graphql-tag';
import { FollowedPublicationsFragment } from '../fragments/followedPublicationsFragment';

export const followedPublicationsQuery = gql`
  query FollowedPublicationsQuery ($input: FollowInput!, $filters: FollowFilterInput!) {
    getFollowedPublications (input: $input, filters: $filters)
    @rest(
      type: "FollowedPublications", 
      path: "/users/{args.input.userOid}/follows/publications?{args.filters}"
    ) {
      ...FollowedPublications
    }
  }
  ${FollowedPublicationsFragment}
`;
