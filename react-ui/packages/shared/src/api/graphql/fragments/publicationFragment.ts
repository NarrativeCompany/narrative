import gql from 'graphql-tag';
import { CurrentUserFollowedItemFragment } from './currentUserFollowedItemFragment';

export const PublicationFragment = gql`
  fragment Publication on Publication {
    oid
    type
    name
    description
    prettyUrlString
    status
    logoUrl
    currentUserFollowedItem @type(name: "CurrentUserFollowedItem") {
      ...CurrentUserFollowedItem
    }
  }
  ${CurrentUserFollowedItemFragment}
`;
