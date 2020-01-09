import { graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { currentUserQuery } from '../graphql/user/currentUserQuery';
import { CurrentUserQuery } from '../../types';
import { infiniteLoadingFixProps } from '../../utils';

interface ParentProps {
  userAuthenticated: boolean;
}

export type WithCurrentUserProps = NamedProps<{currentUserData: GraphqlQueryControls & CurrentUserQuery}, {}>;

export const withCurrentUser = graphql<{}, CurrentUserQuery, {}>(currentUserQuery, {
  // Need to skip if not authenticated since we want to always read the current user from the cache after the initial
  // fetch from the server.  If we don't skip when not authenticated, the result from the query for current user
  // (undefined) will be cached and the query will never execute again since we are using cache-first fetch strategy.
  skip: (ownProps: ParentProps) => !(ownProps.userAuthenticated),
  options: () => ({
    ...infiniteLoadingFixProps,
    fetchPolicy: 'cache-first'
  }),
  name: 'currentUserData'
});
