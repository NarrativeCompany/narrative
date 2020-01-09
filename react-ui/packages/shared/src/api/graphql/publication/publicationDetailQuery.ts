import gql from 'graphql-tag';
import { PublicationDetailFragment } from '../fragments/publicationDetailFragment';

export const publicationDetailQuery = gql`
  query PublicationDetailQuery($publicationId: String!) {
    getPublicationDetail (publicationId: $publicationId)
    @rest(type: "PublicationDetail", path: "/publications/{args.publicationId}") {
      ...PublicationDetail
    }
  }
  ${PublicationDetailFragment}
`;
