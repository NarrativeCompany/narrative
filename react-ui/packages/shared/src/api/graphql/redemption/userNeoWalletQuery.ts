import gql from 'graphql-tag';
import { UserNeoWalletFragment } from '../fragments/userNeoWalletFragment';

export const userNeoWalletQuery = gql`
  query UserNeoWalletQuery {
    getUserNeoWallet @rest(type: "UserNeoWallet", path: "/users/current/neo-wallet") {
      ...UserNeoWallet
    }
  }
  ${UserNeoWalletFragment}
`;
