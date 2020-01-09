import gql from 'graphql-tag';
import { PublicationPowerUserFragment } from '../fragments/publicationPowerUserFragment';

export const publicationPowerUserQuery = gql`
  query PublicationPowerUserQuery($publicationOid: String!, $userOid: String!) {
    getPublicationPowerUser (publicationOid: $publicationOid, userOid: $userOid)
    @rest(type: "PublicationPowerUser", path: "/publications/{args.publicationOid}/power-users/{args.userOid}") {
      ...PublicationPowerUser
    }
  }
  ${PublicationPowerUserFragment}
`;
