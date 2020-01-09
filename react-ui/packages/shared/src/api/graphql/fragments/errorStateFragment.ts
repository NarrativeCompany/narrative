import gql from 'graphql-tag';

// TODO: Must annotate every field with @client due to apollo-link-state# issue #266 - Narrative issue #1012
export const ErrorStateFragment = gql`
  fragment ErrorState on ErrorState {
    type @client
    title @client
    message @client
    referenceId @client
    detail @client
    httpStatusCode @client
    result @client
    data @client
    graphQLErrors @client
    stack @client
  }
`;
