import { DocumentNode } from 'graphql';
import { graphql, MutationFunc, MutationOpts } from 'react-apollo';
import { FetchResult } from 'apollo-link';
import { logException, resolveExceptionFromApolloError } from './errorUtils';

/**
 * Generic mutation implementation to reduce boilerplate in HOCs.
 *
 * This function simply returns a generic mutation implementation function curried with the supplied arguments.
 *
 * @param mutation The mutation for this implementation
 * @param functionName The function name for this implementation
 * @param suppressExceptions true if exceptions should not be thrown from this mutation.  When true, the exception
 * will be logged and swallowed
 * @param <VARTYPE> The variables type for this mutation
 * @param <MUTATIONRESTYPE> The result type
 * @deprecated Should use mutationResolver instead of this!
 */
export const buildMutationImplFunction = <VARTYPE, MUTATIONRESTYPE>(
    mutation: DocumentNode,
    functionName: string,
    suppressExceptions: boolean = false
) =>
    graphql(mutation, {
      props: ({mutate}) => ({
        [functionName]: async (input: VARTYPE) => {
          const options = {
            variables: input,
            mutation
          };

          if (!mutate) {
            throw new Error(functionName + ': missing mutate');
          }

          return mutate(options)
            .then((response: FetchResult<MUTATIONRESTYPE>) => {
              const res =
                response &&
                response.data &&
                response.data[functionName];

              if (!res) {
                throw new Error(functionName + ': no return value from mutation');
              }

              return res;
            }).catch((error) => {
              if (!suppressExceptions) {
                throw resolveExceptionFromApolloError(error);
              }
              logException('Exception suppressed for mutation ' + functionName, error);
            });
        }
      })
    });

export async function mutationResolver <MutationType>(
  mutate: MutationFunc | undefined,
  options: MutationOpts,
  fnName?: string
) {
  if (!mutate) {
    throw new Error (`${fnName}: Missing mutate`);
  }

  return mutate(options)
    .then((response: FetchResult<MutationType>) => {
      let res;

      if (!fnName) {
        res = response && response.data;
      } else {
        res =
          response &&
          response.data &&
          response.data[fnName];
      }

      if (!res) {
        throw new Error(`${fnName}: no return value from mutation`);
      }

      return res;
    }).catch((error) => {
      throw resolveExceptionFromApolloError(error);
    });
}
