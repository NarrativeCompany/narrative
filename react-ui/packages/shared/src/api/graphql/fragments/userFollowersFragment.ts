import gql from 'graphql-tag';
import { FollowedUserFragment } from './followedUserFragment';
import { FollowScrollParamsFragment } from './followScrollParamsFragment';

export const UserFollowersFragment = gql`
  fragment UserFollowers on UserFollowers {
    items @type(name: "FollowedUser") {
      ...FollowedUser
    }
    hasMoreItems
    scrollParams @type(name: "FollowScrollParams") {
      ...FollowScrollParams
    }
    totalFollowers
  }
  ${FollowedUserFragment}
  ${FollowScrollParamsFragment}
`;
