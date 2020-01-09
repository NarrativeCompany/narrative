import { graphql } from 'react-apollo';
import { createPublicationMutation } from '../graphql/publication/createPublicationMutation';
import {
  Publication,
  CreatePublicationInput,
  CreatePublicationMutation,
  CreatePublicationMutationVariables,
} from '../../types';
import { mutationResolver } from '../../utils';

const functionName = 'createPublication';

export interface WithCreatePublicationProps {
  [functionName]: (input: CreatePublicationInput) => Promise<Publication>;
}

export const withCreatePublication =
  graphql<
    {},
    CreatePublicationMutation,
    CreatePublicationMutationVariables,
    WithCreatePublicationProps
  >(createPublicationMutation, {
    props: ({mutate}) => ({
      [functionName]: async (input: CreatePublicationInput) => {

        return await mutationResolver<CreatePublicationMutation>(mutate, {
          variables: {input}
        }, functionName);
      }
    })
  });
