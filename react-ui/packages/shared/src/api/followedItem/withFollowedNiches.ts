import { ChildDataProps, graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { followedNichesQuery } from '../graphql/followedItem/followedNichesQuery';
import { Niche, FollowedNichesQuery } from '../../types';
import {
  createWithFollowsPropsFromQueryResults,
  extractFollowsVariables,
  FollowListParentProps
} from './followListUtils';
import { WithLoadMoreItemsProps } from '../utils';
import { infiniteLoadingFixProps } from '../../utils';

export type WithFollowedNichesProps = WithLoadMoreItemsProps<Niche>;

const queryName = 'followedNichesData';

type WithProps = NamedProps<
  {[queryName]: GraphqlQueryControls & FollowedNichesQuery},
  ChildDataProps<FollowListParentProps, FollowedNichesQuery>
>;

export const withFollowedNiches =
  graphql<
    FollowListParentProps,
    FollowedNichesQuery,
    {},
    WithFollowedNichesProps
  >(followedNichesQuery, {
    options: (props: FollowListParentProps) => ({
      ...infiniteLoadingFixProps,
      variables: extractFollowsVariables(props)
    }),
    name: queryName,
    props: ({ followedNichesData }: WithProps) => {
      return createWithFollowsPropsFromQueryResults('getFollowedNiches', followedNichesData);
    }
  });
