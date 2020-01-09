import gql from 'graphql-tag';
import { UserReferralDetailsFragment } from '../fragments/userReferralDetailsFragment';

export const userReferralDetailsQuery = gql`
  query UserReferralDetailsQuery ($input: UserOidInput!) {
    getUserReferralDetails (input: $input) 
    @rest (type: "UserReferralDetails", path: "/users/{args.input.userOid}/referral-details") {
      ...UserReferralDetails
    }
  }
  ${UserReferralDetailsFragment}
`;
