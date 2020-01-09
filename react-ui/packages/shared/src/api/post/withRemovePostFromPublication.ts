import { graphql } from 'react-apollo';
import { removePostFromPublicationMutation } from '../graphql/post/removePostFromPublicationMutation';
import {
  RemovePostFromPublicationMutation,
  RemovePostFromPublicationMutationVariables,
  RemovePostFromPublicationInput
} from '../../types';
import { mutationResolver } from '../../utils';

const functionName = 'removePostFromPublication';

export interface WithRemovePostFromPublicationProps {
  [functionName]:
    (input: RemovePostFromPublicationInput, postOid: string) => Promise<RemovePostFromPublicationMutation>;
}

export const withRemovePostFromPublication =
  graphql<
    {},
    RemovePostFromPublicationMutation,
    RemovePostFromPublicationMutationVariables,
    WithRemovePostFromPublicationProps
  >(removePostFromPublicationMutation, {
    props: ({mutate}) => ({
      [functionName]: async (input: RemovePostFromPublicationInput, postOid: string) => {
        return await mutationResolver<RemovePostFromPublicationMutation>(mutate, {
          variables: { input, postOid }
        }, functionName);
      }
    })
  });
