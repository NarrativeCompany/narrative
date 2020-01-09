import gql from 'graphql-tag';

export const currentUserTwoFactorAuthStateQuery = gql`
  query CurrentUserTwoFactorAuthStateQuery {
    getCurrentUserTwoFactorAuthState @rest(type: "Result", path: "/users/current/2fa-enabled") {
      enabled: value 
    }
  }
`;
