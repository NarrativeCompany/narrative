import gql from 'graphql-tag';
import { PublicationFragment } from './publicationFragment';
import { UserFragment } from './userFragment';
import { PublicationUrlsFragment } from './publicationUrlsFragment';

export const PublicationDetailFragment = gql`
  fragment PublicationDetail on PublicationDetail {
    oid

    publication @type(name: "Publication") {
      ...Publication
    }

    fathomSiteId
    deletionDatetime

    headerImageUrl
    headerImageAlignment
    
    owner @type(name: "User") {
      ...User
    }
    
    urls @type(name: "PublicationUrls") {
      ...PublicationUrls
    }
    
    currentUserRoles
  }
  ${PublicationFragment}
  ${UserFragment}
  ${PublicationUrlsFragment}
`;
