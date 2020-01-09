import gql from 'graphql-tag';
import { NeoWalletFragment } from '../fragments/neoWalletFragment';

export const neoWalletsQuery = gql`
  query NeoWalletsQuery {
    getNeoWallets @rest(type: "NeoWallet", path: "/neo-wallets") {
      ...NeoWallet
    }
  }
  ${NeoWalletFragment}
`;
