import { graphql } from 'react-apollo';
import { deletePublicationPowerUserMutation } from '../graphql/publication/deletePublicationPowerUserMutation';
import {
  PublicationPowerUsers,
  DeletePublicationPowerUserInput,
  DeletePublicationPowerUserMutation,
  DeletePublicationPowerUserMutationVariables,
} from '../../types';
import { mutationResolver } from '../../utils';

const functionName = 'deletePublicationPowerUser';

export interface WithDeletePublicationPowerUserProps {
  [functionName]: (input: DeletePublicationPowerUserInput) => Promise<PublicationPowerUsers>;
}

export const withDeletePublicationPowerUser =
  graphql<
    {},
    DeletePublicationPowerUserMutation,
    DeletePublicationPowerUserMutationVariables,
    WithDeletePublicationPowerUserProps
  >(deletePublicationPowerUserMutation, {
    props: ({mutate}) => ({
      [functionName]: async (input: DeletePublicationPowerUserInput) => {
        return await mutationResolver<DeletePublicationPowerUserMutation>(mutate, {
          variables: { input }
        }, functionName);
      }
    })
  });
