import { graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { allTribunalMembersQuery } from '../graphql/tribunalMember/allTribunalMembersQuery';
import { AllTribunalMembersQuery } from '../../types';

export type WithAllTribunalMembersProps =
  NamedProps<{allTribunalMembersData: GraphqlQueryControls & AllTribunalMembersQuery}, {}>;

export const withAllTribunalMembers = graphql<{}, AllTribunalMembersQuery>(allTribunalMembersQuery, {
  name: 'allTribunalMembersData'
});
