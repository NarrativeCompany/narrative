import {
  DeleteUserNeoWalletInput,
  DeleteUserNeoWalletMutation,
  UserNeoWallet,
  DeleteUserNeoWalletMutationVariables
} from '../../types';
import { mutationResolver } from '../../utils';
import { deleteUserNeoWalletMutation } from '../graphql/redemption/deleteUserNeoWalletMutation';
import { graphql } from 'react-apollo';

const functionName = 'deleteUserNeoWallet';

export interface WithDeleteUserNeoWalletProps {
  [functionName]: (input: DeleteUserNeoWalletInput) => Promise<UserNeoWallet>;
}

export const withDeleteUserNeoWallet =
  graphql<
    {},
    DeleteUserNeoWalletMutation,
    DeleteUserNeoWalletMutationVariables,
    WithDeleteUserNeoWalletProps
  >(deleteUserNeoWalletMutation, {
    props: ({mutate}) => ({
      [functionName]: async (input: DeleteUserNeoWalletInput) => {
        return await mutationResolver<DeleteUserNeoWalletMutation>(mutate, {
          variables: { input }
        }, functionName);
      }
    })
  });
