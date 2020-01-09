import gql from 'graphql-tag';

export const UserReferralDetailsFragment = gql`
  fragment UserReferralDetails on UserReferralDetails {
    rank
    friendsJoined
    nrveEarned
  }
`;
