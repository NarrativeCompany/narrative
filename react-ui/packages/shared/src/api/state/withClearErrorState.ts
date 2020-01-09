import { graphql } from 'react-apollo';
import { FetchResult } from 'apollo-link';
import { ClearErrorStateMutation } from '../../types';
import { clearErrorStateMutation } from '../graphql/error';
import { resolveExceptionFromApolloError } from '../../utils';

export interface WithClearErrorStateProps {
  clearErrorState: () => Promise<boolean>;
}

export const withClearErrorState = graphql(clearErrorStateMutation, {
  props: ({mutate}) => ({
    clearErrorState: async () => {
      const variables = {};
      const options = {
        variables
      };

      if (!mutate) {
        throw new Error('withClearErrorState: missing mutate');
      }

      return mutate(options)
        .then((response: FetchResult<ClearErrorStateMutation>) => {
          return response &&
            response.data &&
            response.data.clearErrorState;
        }).catch((error) => {
          throw resolveExceptionFromApolloError(error);
        });
    }
  })
});
