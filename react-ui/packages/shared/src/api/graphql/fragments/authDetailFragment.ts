import gql from 'graphql-tag';

export const AuthDetailFragment = gql`
  fragment AuthDetail on AuthPayload {
    token
    twoFactorAuthExpired
  }
`;
