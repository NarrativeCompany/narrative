import { graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { nicheUserAssociationsQuery } from '../graphql/user/nicheUserAssociationsQuery';
import { NicheUserAssociationsQuery, NicheUserAssociationsQueryVariables, NicheUserAssociation } from '../../types';
import { LoadingProps } from '../../utils';

interface ParentProps {
  userOid: string;
}

export interface WithNicheUserAssociationsProps extends LoadingProps {
  associations: NicheUserAssociation[];
}

const queryName = 'nicheUserAssociationsData';

type Props = NamedProps<{[queryName]: GraphqlQueryControls & NicheUserAssociationsQuery}, ParentProps>;

export const withNicheUserAssociations =
  graphql<
    ParentProps,
    NicheUserAssociationsQuery,
    NicheUserAssociationsQueryVariables,
    WithNicheUserAssociationsProps
  >(nicheUserAssociationsQuery, {
    options: ({userOid}) => ({
      variables: {
        userOid
      }
    }),
    name: queryName,
    props: ({ nicheUserAssociationsData }: Props): WithNicheUserAssociationsProps => {
      const { loading, getNicheUserAssociations } = nicheUserAssociationsData;
      const associations = getNicheUserAssociations || [];

      return { loading, associations };
    }
  });
