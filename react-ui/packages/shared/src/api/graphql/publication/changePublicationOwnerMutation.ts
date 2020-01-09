import gql from 'graphql-tag';
import { PublicationPowerUsersFragment } from '../fragments/publicationPowerUsersFragment';

export const changePublicationOwnerMutation = gql`
  mutation ChangePublicationOwnerMutation ($input: ChangePublicationOwnerInput!, $publicationOid: String!) {
    changePublicationOwner (input: $input, publicationOid: $publicationOid) @rest(
      type: "PublicationPowerUsers", 
      path: "/publications/{args.publicationOid}/owner", 
      method: "PUT"
    ) {
      ...PublicationPowerUsers
    }
  }
  ${PublicationPowerUsersFragment}
`;
