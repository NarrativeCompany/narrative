import gql from 'graphql-tag';
import { TopNicheFragment } from '../fragments/topNicheFragment';

export const publicationTopNichesQuery = gql`
  query PublicationTopNichesQuery($publicationOid: String!) {
    getPublicationTopNiches (publicationOid: $publicationOid)
    @rest(type: "TopNiche", path: "/publications/{args.publicationOid}/top-niches") {
      ...TopNiche
    }
  }
  ${TopNicheFragment}
`;
