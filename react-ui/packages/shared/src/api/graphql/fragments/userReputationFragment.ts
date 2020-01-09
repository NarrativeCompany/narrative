import gql from 'graphql-tag';

export const UserReputationFragment = gql`
  fragment UserReputation on UserReputation {
    oid
    conductNegative
    negativeConductExpirationTimestamp
    qualityAnalysisScore
    kycVerifiedScore
    kycVerificationPending
    totalScore
    level
  }
`;
