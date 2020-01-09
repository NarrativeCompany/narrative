import gql from 'graphql-tag';
import { PublicationSettingsFragment } from '../fragments/publicationSettingsFragment';

export const updatePublicationSettingsMutation = gql`
  mutation UpdatePublicationSettingsMutation ($input: PublicationSettingsInput!, $publicationOid: String!) {
    updatePublicationSettings (input: $input, publicationOid: $publicationOid) @rest(
      type: "PublicationSettings", 
      path: "/publications/{args.publicationOid}/settings", 
      method: "PUT"
    ) {
      ...PublicationSettings
    }
  }
  ${PublicationSettingsFragment}
`;
