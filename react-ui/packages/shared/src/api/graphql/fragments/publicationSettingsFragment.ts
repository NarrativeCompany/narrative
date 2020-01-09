import gql from 'graphql-tag';
import { PublicationDetailFragment } from './publicationDetailFragment';

export const PublicationSettingsFragment = gql`
  fragment PublicationSettings on PublicationSettings {
    oid

    publicationDetail @type(name: "PublicationDetail") {
      ...PublicationDetail
    }
    
    contentRewardWriterShare
    contentRewardRecipient
  }
  ${PublicationDetailFragment}
`;
