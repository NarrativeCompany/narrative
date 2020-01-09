import { graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { currentUserTwoFactorAuthStateQuery } from '../graphql/user/currentUserTwoFactorAuthStateQuery';
import { CurrentUserTwoFactorAuthStateQuery } from '../../types';

export type WithCurrentUserTwoFactorAuthStateProps =
  NamedProps<{currentUserTwoFactorAuthStateData: GraphqlQueryControls & CurrentUserTwoFactorAuthStateQuery}, {}>;

export const withCurrentUserTwoFactorAuthState =
  graphql<{}, CurrentUserTwoFactorAuthStateQuery, {}>(currentUserTwoFactorAuthStateQuery, {
    name: 'currentUserTwoFactorAuthStateData'
  });
