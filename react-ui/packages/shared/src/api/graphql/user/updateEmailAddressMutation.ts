import gql from 'graphql-tag';
import { UserEmailAddressDetailFragment } from '../fragments/userEmailAddressDetailFragment';

export const updateEmailAddressMutation = gql`
  mutation UpdateEmailAddressMutation ($input: UpdateEmailAddressInput!) {
    updateEmailAddress (input: $input) @rest(
      type: "UserEmailAddressDetail", 
      path: "/users/current/email-address", 
      method: "PUT"
    ) {
      ...UserEmailAddressDetail
    }
  }
  ${UserEmailAddressDetailFragment}
`;
