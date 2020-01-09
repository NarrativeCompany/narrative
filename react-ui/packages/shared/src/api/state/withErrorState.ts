import { graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { AuthStateQuery, ErrorStateQuery } from '../../types';
import { errorStateQuery } from '../graphql/error';

export type WithErrorStateProps = NamedProps<{errorStateData: GraphqlQueryControls & ErrorStateQuery}, {}>;

export const withErrorState = graphql<AuthStateQuery>(errorStateQuery, {
  options: () => ({
    // TODO: Don't understand why but cache-only is unstable for this link state only query so use cache-first
    fetchPolicy: 'cache-first'
  }),
  name: 'errorStateData'
});
