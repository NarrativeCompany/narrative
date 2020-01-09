import gql from 'graphql-tag';
import { UserKycFragment } from '../fragments/userKycFragment';

export const currentUserKycQuery = gql`
  query CurrentUserKycQuery {
    getCurrentUserKyc @rest(type: "UserKyc", path: "/users/current/kyc/status") {
      ...UserKyc
    }
  }
  ${UserKycFragment}
`;
