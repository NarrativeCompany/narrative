import { graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { currentUserDetailQuery } from '../graphql/user/currentUserDetailQuery';
import { CurrentUserDetailQuery } from '../../types';

export type WithCurrentUserDetailProps =
  NamedProps<{currentUserDetailData: GraphqlQueryControls & CurrentUserDetailQuery}, {}>;

export const withCurrentUserDetail = graphql<{}, CurrentUserDetailQuery>(currentUserDetailQuery, {
  options: () => ({}),
  name: 'currentUserDetailData'
});
