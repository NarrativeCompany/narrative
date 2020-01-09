import { graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { publicationPowerUsersQuery } from '../graphql/publication/publicationPowerUsersQuery';
import { PublicationPowerUsers, PublicationPowerUsersQuery } from '../../types';
import { LoadingProps } from '../../utils';

// jw: per new standards, let's trim the fat on what we are exposing and limit it to just what we care about:
export interface WithPublicationPowerUsersProps extends LoadingProps {
  publicationPowerUsers: PublicationPowerUsers;
}

export interface WithPublicationPowerUsersParentProps {
  publicationOid: string;
}

const queryName = 'publicationPowerUsersData';

type Props = NamedProps<
    {[queryName]: GraphqlQueryControls & PublicationPowerUsersQuery},
    WithPublicationPowerUsersParentProps
  >;

export const withPublicationPowerUsers =
  graphql<
    WithPublicationPowerUsersParentProps,
    PublicationPowerUsersQuery,
    {},
    WithPublicationPowerUsersProps
  >(publicationPowerUsersQuery, {
    name: queryName,
    options: ({publicationOid}) => ({
      variables: {
        publicationOid
      }
    }),
    props: ({ publicationPowerUsersData }: Props): WithPublicationPowerUsersProps => {
      const { loading } = publicationPowerUsersData;
      const publicationPowerUsers = publicationPowerUsersData.getPublicationPowerUsers;

      return { loading, publicationPowerUsers };
    }
  });
