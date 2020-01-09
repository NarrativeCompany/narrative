import { ChildDataProps, graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { followedUsersQuery } from '../graphql/followedItem/followedUsersQuery';
import { FollowedUser, FollowedUsersQuery } from '../../types';
import {
  createWithFollowsPropsFromQueryResults,
  extractFollowsVariables,
  FollowListParentProps
} from './followListUtils';
import { WithLoadMoreItemsProps } from '../utils';
import { infiniteLoadingFixProps } from '../../utils';

export type WithFollowedUsersProps = WithLoadMoreItemsProps<FollowedUser>;

const queryName = 'followedUsersData';

type WithProps = NamedProps<
  {[queryName]: GraphqlQueryControls & FollowedUsersQuery},
  ChildDataProps<FollowListParentProps, FollowedUsersQuery>
>;

export const withFollowedUsers =
  graphql<
    FollowListParentProps,
    FollowedUsersQuery,
    {},
    WithFollowedUsersProps
  >(followedUsersQuery, {
    options: (props: FollowListParentProps) => ({
      ...infiniteLoadingFixProps,
      variables: extractFollowsVariables(props)
    }),
    name: queryName,
    props: ({ followedUsersData }: WithProps) => {
      return createWithFollowsPropsFromQueryResults('getFollowedUsers', followedUsersData);
    }
  });
