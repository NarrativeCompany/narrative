import gql from 'graphql-tag';

export const recaptchaPublicKeyQuery = gql`
  query RecaptchaPublicKeyQuery {
    getRecaptchaPublicKey @rest(type: "RecaptchaPublicKey", path: "/config/recaptcha-public-key") {
      publicKey: value
    }
  }
`;
