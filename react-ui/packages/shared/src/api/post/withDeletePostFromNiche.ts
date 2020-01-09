import { graphql } from 'react-apollo';
import { deletePostFromNicheMutation } from '../graphql/post/deletePostFromNicheMutation';
import { DeletePostFromNicheMutation, DeletePostFromNicheMutationVariables } from '../../types';
import { mutationResolver } from '../../utils';

const functionName = 'deletePostFromNiche';

export interface WithDeletePostFromNicheProps {
  [functionName]: (postOid: string, nicheOid: string) => Promise<DeletePostFromNicheMutation>;
}

export const withDeletePostFromNiche =
  graphql<
    {},
    DeletePostFromNicheMutation,
    DeletePostFromNicheMutationVariables,
    WithDeletePostFromNicheProps
  >(deletePostFromNicheMutation, {
    props: ({mutate}) => ({
      [functionName]: async (postOid: string, nicheOid: string) => {
        return await mutationResolver<DeletePostFromNicheMutation>(mutate, {
          variables: { postOid, nicheOid }
        }, functionName);
      }
    })
  });
