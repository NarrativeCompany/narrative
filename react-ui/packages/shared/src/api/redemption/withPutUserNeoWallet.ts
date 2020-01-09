import { graphql } from 'react-apollo';
import { putUserNeoWalletMutation } from '../graphql/redemption/putUserNeoWalletMutation';
import {
  UserNeoWallet,
  UpdateUserNeoWalletInput,
  PutUserNeoWalletMutation,
  PutUserNeoWalletMutationVariables,
} from '../../types';
import { mutationResolver } from '../../utils';

const functionName = 'putUserNeoWallet';

export interface WithPutUserNeoWalletProps {
  [functionName]: (input: UpdateUserNeoWalletInput) => Promise<UserNeoWallet>;
}

export const withPutUserNeoWallet =
  graphql<
    {},
    PutUserNeoWalletMutation,
    PutUserNeoWalletMutationVariables,
    WithPutUserNeoWalletProps
  >(putUserNeoWalletMutation, {
    props: ({mutate}) => ({
      [functionName]: async (input: UpdateUserNeoWalletInput) => {
        return await mutationResolver<PutUserNeoWalletMutation>(mutate, {
          variables: { input }
        }, functionName);
      }
    })
  });
