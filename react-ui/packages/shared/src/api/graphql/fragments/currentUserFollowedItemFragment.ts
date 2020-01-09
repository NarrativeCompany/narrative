import gql from 'graphql-tag';

export const CurrentUserFollowedItemFragment = gql`
  fragment CurrentUserFollowedItem on CurrentUserFollowedItem {
    oid
    followed
  }
`;
