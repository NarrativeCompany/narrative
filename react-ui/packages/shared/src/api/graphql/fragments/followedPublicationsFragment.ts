import gql from 'graphql-tag';
import { PublicationFragment } from './publicationFragment';
import { FollowScrollParamsFragment } from './followScrollParamsFragment';

export const FollowedPublicationsFragment = gql`
  fragment FollowedPublications on FollowedPublications {
    items @type(name: "Publication") {
      ...Publication
    }
    hasMoreItems
    scrollParams @type(name: "FollowScrollParams") {
      ...FollowScrollParams
    }
  }
  ${PublicationFragment}
  ${FollowScrollParamsFragment}
`;
