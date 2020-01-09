import gql from 'graphql-tag';
import { NicheFragment } from './nicheFragment';
import { FollowScrollParamsFragment } from './followScrollParamsFragment';

export const FollowedNichesFragment = gql`
  fragment FollowedNiches on FollowedNiches {
    items @type(name: "Niche") {
      ...Niche
    }
    hasMoreItems
    scrollParams @type(name: "FollowScrollParams") {
      ...FollowScrollParams
    }
  }
  ${NicheFragment}
  ${FollowScrollParamsFragment}
`;
