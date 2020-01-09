import gql from 'graphql-tag';
import { PublicationPlanDetailFragment } from '../fragments/publicationPlanDetailFragment';

export const publicationPlanDetailQuery = gql`
  query PublicationPlanDetailQuery($publicationOid: String!) {
    getPublicationPlanDetail (publicationOid: $publicationOid)
    @rest(type: "PublicationPlanDetail", path: "/publications/{args.publicationOid}/plan") {
      ...PublicationPlanDetail
    }
  }
  ${PublicationPlanDetailFragment}
`;
