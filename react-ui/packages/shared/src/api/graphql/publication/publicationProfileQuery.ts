import gql from 'graphql-tag';
import { PublicationProfileFragment } from '../fragments/publicationProfileFragment';

export const publicationProfileQuery = gql`
  query PublicationProfileQuery($publicationOid: String!) {
    getPublicationProfile (publicationOid: $publicationOid)
    @rest(type: "PublicationProfile", path: "/publications/{args.publicationOid}/profile") {
      ...PublicationProfile
    }
  }
  ${PublicationProfileFragment}
`;
