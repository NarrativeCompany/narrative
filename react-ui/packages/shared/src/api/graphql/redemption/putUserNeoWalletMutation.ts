import gql from 'graphql-tag';
import { UserNeoWalletFragment } from '../fragments/userNeoWalletFragment';

export const putUserNeoWalletMutation = gql`
  mutation PutUserNeoWalletMutation ($input: UpdateUserNeoWalletInput!) {
    putUserNeoWallet (input: $input) @rest(type: "UserNeoWallet", path: "/users/current/neo-wallet", method: "PUT") {
      ...UserNeoWallet
    }
  }
  ${UserNeoWalletFragment}
`;
