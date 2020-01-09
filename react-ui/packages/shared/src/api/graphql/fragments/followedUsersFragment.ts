import gql from 'graphql-tag';
import { FollowedUserFragment } from './followedUserFragment';
import { FollowScrollParamsFragment } from './followScrollParamsFragment';

export const FollowedUsersFragment = gql`
  fragment FollowedUsers on FollowedUsers {
    items @type(name: "FollowedUser") {
      ...FollowedUser
    }
    hasMoreItems
    scrollParams @type(name: "FollowScrollParams") {
      ...FollowScrollParams
    }
  }
  ${FollowedUserFragment}
  ${FollowScrollParamsFragment}
`;
