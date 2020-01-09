import gql from 'graphql-tag';
import { UserDetailFragment } from './userDetailFragment';
import { NicheFragment } from './nicheFragment';
import { PublicationFragment } from './publicationFragment';
import { PostFragment } from './postFragment';

export const SearchResultFragment = gql`
  fragment SearchResult on SearchResult {
    oid

    userDetail @type(name: "UserDetail") {
      ...UserDetail
    }
    niche @type(name: "Niche") {
      ...Niche
    }
    publication @type(name: "Publication") {
      ...Publication
    }
    post @type(name: "Post") {
      ...Post
    }
  }
  ${UserDetailFragment}
  ${NicheFragment}
  ${PublicationFragment}
  ${PostFragment}
`;
