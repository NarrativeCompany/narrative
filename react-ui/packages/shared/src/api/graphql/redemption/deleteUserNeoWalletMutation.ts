import gql from 'graphql-tag';
import { UserNeoWalletFragment } from '../fragments/userNeoWalletFragment';

export const deleteUserNeoWalletMutation = gql`
  mutation DeleteUserNeoWalletMutation ($input: DeleteUserNeoWalletInput!) {
    deleteUserNeoWallet (input: $input) @rest(
      type: "UserNeoWallet", 
      path: "/users/current/neo-wallet/delete", 
      method: "POST"
    ) {
      ...UserNeoWallet
    }
  }
  ${UserNeoWalletFragment}
`;
