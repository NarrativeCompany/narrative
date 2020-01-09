import gql from 'graphql-tag';
import { PublicationPowerUsersFragment } from '../fragments/publicationPowerUsersFragment';

export const publicationPowerUsersQuery = gql`
  query PublicationPowerUsersQuery($publicationOid: String!) {
    getPublicationPowerUsers (publicationOid: $publicationOid)
    @rest(type: "PublicationPowerUsers", path: "/publications/{args.publicationOid}/power-users") {
      ...PublicationPowerUsers
    }
  }
  ${PublicationPowerUsersFragment}
`;
