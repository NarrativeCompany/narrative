import { DocumentNode } from 'graphql';

export interface ApolloCacheParams {
  // TODO: need to figure out type for the store (potentially DataProxy?)
  // tslint:disable-next-line no-any
  store: any;
  query?: DocumentNode;
  queryVariables?: {
    // tslint:disable-next-line no-any
    [key: string]: any
  };
}

export type ApolloCacheReadParams =
  ApolloCacheParams;

export type ApolloCacheWriteParams =
  ApolloCacheParams & {
  // tslint:disable-next-line no-any
  data: any;
};

export function readDataFromApolloCache (params: ApolloCacheReadParams) {
  const { store, query, queryVariables } = params;

  return store.readQuery({
    query,
    variables: queryVariables
  });
}

export function writeDataToApolloCache (params: ApolloCacheWriteParams) {
  const { store, query, queryVariables, data } = params;

  store.writeQuery({
    query,
    data,
    variables: queryVariables
  });

  return;
}
