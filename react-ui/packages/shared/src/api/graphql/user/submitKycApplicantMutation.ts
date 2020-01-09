import gql from 'graphql-tag';
import { UserKycFragment } from '../fragments/userKycFragment';

export const submitKycApplicantMutation = gql`
  mutation SubmitKycApplicantMutation ($input: KycApplicationInput!, $bodySerializer: Any!) {
    submitKycApplicant (input: $input) @rest(
      type: "UserKyc",
      path: "/users/current/kyc/verification",
      method: "POST",
      bodySerializer: $bodySerializer
    ) {
      ...UserKyc
    }
  }
  ${UserKycFragment}
`;
