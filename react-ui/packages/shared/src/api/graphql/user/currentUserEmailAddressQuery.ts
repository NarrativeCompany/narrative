import gql from 'graphql-tag';
import { UserEmailAddressDetailFragment } from '../fragments/userEmailAddressDetailFragment';

export const currentUserEmailAddressQuery = gql`
  query CurrentUserEmailAddressQuery {
    getCurrentUserEmailAddress @rest(type: "UserEmailAddressDetail", path: "/users/current/email-address") {
      ...UserEmailAddressDetail
    }
  }
  ${UserEmailAddressDetailFragment}
`;
