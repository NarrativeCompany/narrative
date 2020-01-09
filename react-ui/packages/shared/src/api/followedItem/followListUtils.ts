import { FollowedUser, Niche } from '../../types';
import { GraphqlQueryControls } from 'react-apollo';
import { createLoadMorePropsFromQueryResults, ScrollableLoadMoreItems, WithLoadMoreItemsProps } from '../utils';

const resultsPerPage = 25;

export interface FollowListParentProps {
  count?: number;
  userOid: string;
}

interface FollowScrollParams {
  lastItemName: string;
  lastItemOid: string;
}

export type FollowableObject = Niche | FollowedUser;

export interface WithFollowsProps<T extends FollowableObject> extends WithLoadMoreItemsProps<T> {
  // jw: nothing to do here.
}

export interface Follows<T extends FollowableObject>
  extends ScrollableLoadMoreItems<T, FollowScrollParams>
{

}

// jw: due to how properties in TypeScript work, while the compiler limits us to what we have defined in our functional
//     scope, whatever was on the parent is going to be present on the props. So just because we do not see other
//     properties does not mean they aren't there. If we expand the props onto "variables.filters" then we will get all
//     properties, including the hidden ones, included in the rest request. So, let's extract just the properties we
//     need, and leave the rest behind.
export function extractFollowsVariables(props: FollowListParentProps) {
  const { count, userOid } = props;

  // jw: let's ensure we specify a default count here
  return {
    input: { userOid },
    filters: { count: count || resultsPerPage }
  };
}

// jw: This utility function will parse the results from a Query, and massage them into the WithContentStreamProps
export function createWithFollowsPropsFromQueryResults
  <
    DataKey extends keyof QueryResult,
    QueryResult extends GraphqlQueryControls,
    T extends FollowableObject,
    D extends Follows<T>,
    R extends WithFollowsProps<T>
  >
  (
    dataKey: DataKey,
    queryResults: QueryResult,
    extraDataExtractor?: (data: D) => {}
  ): R
{
  return createLoadMorePropsFromQueryResults<DataKey, QueryResult, T, FollowScrollParams, D, R>(
    dataKey,
    queryResults,
    { extraDataExtractor }
  );
}
