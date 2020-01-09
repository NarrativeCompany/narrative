import gql from 'graphql-tag';

export const generateTwoFactorSecretQuery = gql`
  query GenerateTwoFactorSecretQuery {
    getGeneratedTwoFactorSecret @rest (type: "TwoFactorSecret", path: "/users/current/2fa-secret") {
      secret
      qrCodeImage
      backupCodes
    }
  }
`;
