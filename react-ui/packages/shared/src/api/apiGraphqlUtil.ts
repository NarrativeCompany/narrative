import { ApolloClient } from 'apollo-client';
import { InMemoryCache, NormalizedCacheObject } from 'apollo-cache-inmemory';
import { RestLink } from 'apollo-link-rest';
import { ApolloLink } from 'apollo-link';
import { onError } from 'apollo-link-error';
import { setContext } from 'apollo-link-context';

// TODO: Apollo client will potentially be initialized in individual packages instead of shared

const restLink = new RestLink({
  uri: '/api'
});

const errorLink = onError(({graphQLErrors, networkError, operation, response}) => {
  // TODO: If we keep apollo-client here this needs to be removed
  // tslint:disable no-console
  console.error('graphql errors', graphQLErrors);
  console.error('network errors', networkError);
  console.error('operation', operation);
  console.error('response', response);
  // tslint:enable no-console
});

// const httpLink = new HttpLink({
//   uri: '/graphql'
// });

const createAuthLink = (token: string | null) => setContext((_, { headers }) => {
  return {
    headers: {
      ...headers,
      authorization: token ? `Bearer ${token}` : '',
    }
  };
});

export const apolloClient = new ApolloClient({
  link: ApolloLink.from([restLink]),
  cache: new InMemoryCache()
});

export const initApolloClient = (token: string | null): ApolloClient<NormalizedCacheObject>  => {
  const authLink = createAuthLink(token);

  return new ApolloClient({
    link: ApolloLink.from([errorLink, authLink, restLink]),
    cache: new InMemoryCache(),
  });
};
