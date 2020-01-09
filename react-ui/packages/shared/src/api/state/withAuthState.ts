import { graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { AuthStateQuery } from '../../types';
import { authStateQuery } from '../graphql/state/authStateQuery';

export type WithAuthStateProps = NamedProps<{authStateData: GraphqlQueryControls & AuthStateQuery}, {}>;

export const withAuthState = graphql<AuthStateQuery>(authStateQuery, {
  options: () => ({
    // TODO: Don't understand why but cache-only is unstable for this link state only query so use cache-first
    fetchPolicy: 'cache-first'
  }),
  name: 'authStateData'
});
