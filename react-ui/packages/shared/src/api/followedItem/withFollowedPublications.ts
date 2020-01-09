import { ChildDataProps, graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { followedPublicationsQuery } from '../graphql/followedItem/followedPublicationsQuery';
import { Niche, FollowedPublicationsQuery } from '../../types';
import {
  createWithFollowsPropsFromQueryResults,
  extractFollowsVariables,
  FollowListParentProps
} from './followListUtils';
import { WithLoadMoreItemsProps } from '../utils';
import { infiniteLoadingFixProps } from '../../utils';

export type WithFollowedPublicationsProps = WithLoadMoreItemsProps<Niche>;

const queryName = 'followedPublicationsData';

type WithProps = NamedProps<
  {[queryName]: GraphqlQueryControls & FollowedPublicationsQuery},
  ChildDataProps<FollowListParentProps, FollowedPublicationsQuery>
>;

export const withFollowedPublications =
  graphql<
    FollowListParentProps,
    FollowedPublicationsQuery,
    {},
    WithFollowedPublicationsProps
  >(followedPublicationsQuery, {
    options: (props: FollowListParentProps) => ({
      ...infiniteLoadingFixProps,
      variables: extractFollowsVariables(props)
    }),
    name: queryName,
    props: ({ followedPublicationsData }: WithProps) => {
      return createWithFollowsPropsFromQueryResults('getFollowedPublications', followedPublicationsData);
    }
  });
