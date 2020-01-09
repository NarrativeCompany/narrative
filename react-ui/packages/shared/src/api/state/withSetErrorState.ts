import { graphql } from 'react-apollo';
import { FetchResult } from 'apollo-link';
import { ErrorStateInput, SetErrorStateMutation, } from '../../types';
import { setErrorStateMutation } from '../graphql/error';
import { resolveExceptionFromApolloError } from '../../utils';

export interface WithSetErrorStateProps {
  setErrorState: (input: ErrorStateInput) => Promise<boolean>;
}

export const withSetErrorState = graphql(setErrorStateMutation, {
  props: ({mutate}) => ({
    setErrorState: async (input: ErrorStateInput) => {
      const variables = {input};
      const options = {
        variables
      };

      if (!mutate) {
        throw new Error('withSetErrorState: missing mutate');
      }

      return mutate(options)
        .then((response: FetchResult<SetErrorStateMutation>) => {
          return response &&
            response.data &&
            response.data.setErrorState;
        }).catch((error) => {
          throw resolveExceptionFromApolloError(error);
        });
    }
  })
});
