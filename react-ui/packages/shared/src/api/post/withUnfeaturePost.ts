import { graphql } from 'react-apollo';
import { unfeaturePostMutation } from '../graphql/post/unfeaturePostMutation';
import { UnfeaturePostMutation, UnfeaturePostMutationVariables } from '../../types';
import { mutationResolver } from '../../utils';

const functionName = 'unfeaturePost';

export interface WithUnfeaturePostProps {
  [functionName]: (postOid: string) => Promise<UnfeaturePostMutation>;
}

export const withUnfeaturePost =
  graphql<
    {},
    UnfeaturePostMutation,
    UnfeaturePostMutationVariables,
    WithUnfeaturePostProps
  >(unfeaturePostMutation, {
    props: ({mutate}) => ({
      [functionName]: async (postOid: string) => {
        return await mutationResolver<UnfeaturePostMutation>(mutate, {
          variables: { postOid }
        }, functionName);
      }
    })
  });
