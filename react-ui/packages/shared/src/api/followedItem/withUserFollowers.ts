import { ChildDataProps, graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { userFollowersQuery } from '../graphql/followedItem/userFollowersQuery';
import { FollowedUser, UserFollowers, UserFollowersQuery } from '../../types';
import {
  createWithFollowsPropsFromQueryResults,
  extractFollowsVariables,
  FollowListParentProps
} from './followListUtils';
import { WithLoadMoreItemsProps } from '../utils';
import { infiniteLoadingFixProps } from '../../utils';

export interface WithUserFollowersProps extends WithLoadMoreItemsProps<FollowedUser> {
  totalFollowers: number;
}

const queryName = 'userFollowersData';

type WithProps = NamedProps<
  {[queryName]: GraphqlQueryControls & UserFollowersQuery},
  ChildDataProps<FollowListParentProps, UserFollowersQuery>
>;

export const withUserFollowers =
  graphql<
    FollowListParentProps,
    UserFollowersQuery,
    {},
    WithUserFollowersProps
  >(userFollowersQuery, {
    options: (props: FollowListParentProps) => ({
      ...infiniteLoadingFixProps,
      variables: extractFollowsVariables(props)
    }),
    name: queryName,
    props: ({ userFollowersData }: WithProps) => {
      return createWithFollowsPropsFromQueryResults(
        'getUserFollowers',
        userFollowersData,
        // jw: similar to contentStreamUtils, there is an issue with the `__typename` property that I'm not sure how to
        //     solve, but does not cause any issues with fetching the data.
        // @ts-ignore
        (data: UserFollowers) => {
          const totalFollowers = data && data.totalFollowers;

          return { totalFollowers };
        }
      );
    }
  });
