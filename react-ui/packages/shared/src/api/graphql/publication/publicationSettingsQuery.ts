import gql from 'graphql-tag';
import { PublicationSettingsFragment } from '../fragments/publicationSettingsFragment';

export const publicationSettingsQuery = gql`
  query PublicationSettingsQuery($publicationOid: String!) {
    getPublicationSettings (publicationOid: $publicationOid)
    @rest(type: "PublicationSettings", path: "/publications/{args.publicationOid}/settings") {
      ...PublicationSettings
    }
  }
  ${PublicationSettingsFragment}
`;
