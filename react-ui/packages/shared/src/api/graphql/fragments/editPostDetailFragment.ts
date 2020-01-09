import gql from 'graphql-tag';
import { PostDetailFragment } from './postDetailFragment';
import { PublicationFragment } from './publicationFragment';
import { PublicationDetailFragment } from './publicationDetailFragment';

export const EditPostDetailFragment = gql`
  fragment EditPostDetail on EditPostDetail {
    postDetail @type(name: "PostDetail") {
      ...PostDetail
    }
    rawBody
    authorAgeRating
    edit
    authorPersonalJournalOid
    availablePublications @type(name: "Publication") {
      ...Publication
    }
    blockedInNicheOids
    publishedToPublicationDetail @type(name: "PublicationDetail") {
      ...PublicationDetail
    }
  }
  ${PostDetailFragment}
  ${PublicationFragment}
  ${PublicationDetailFragment}
`;
