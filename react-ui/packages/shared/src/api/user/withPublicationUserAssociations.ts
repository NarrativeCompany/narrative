import { graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { publicationUserAssociationsQuery } from '../graphql/user/publicationUserAssociationsQuery';
import {
  PublicationUserAssociationsQuery,
  PublicationUserAssociationsQueryVariables,
  PublicationUserAssociation
} from '../../types';
import { LoadingProps } from '../../utils';

interface ParentProps {
  userOid: string;
}

export interface WithPublicationUserAssociationsProps extends LoadingProps {
  associations: PublicationUserAssociation[];
}

const queryName = 'publicationUserAssociationsData';

type Props = NamedProps<{[queryName]: GraphqlQueryControls & PublicationUserAssociationsQuery}, ParentProps>;

export const withPublicationUserAssociations =
  graphql<
    ParentProps,
    PublicationUserAssociationsQuery,
    PublicationUserAssociationsQueryVariables,
    WithPublicationUserAssociationsProps
  >(publicationUserAssociationsQuery, {
    options: ({userOid}) => ({
      variables: {
        userOid
      }
    }),
    name: queryName,
    props: ({ publicationUserAssociationsData }: Props): WithPublicationUserAssociationsProps => {
      const { loading, getPublicationUserAssociations } = publicationUserAssociationsData;
      const associations = getPublicationUserAssociations || [];

      return { loading, associations };
    }
  });
