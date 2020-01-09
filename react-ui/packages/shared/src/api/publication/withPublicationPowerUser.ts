import { graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { publicationPowerUserQuery } from '../graphql/publication/publicationPowerUserQuery';
import { PublicationPowerUser, PublicationPowerUserQuery } from '../../types';
import { LoadingProps } from '../../utils';

// jw: per new standards, let's trim the fat on what we are exposing and limit it to just what we care about:
export interface WithPublicationPowerUserProps extends LoadingProps {
  publicationPowerUser: PublicationPowerUser;
}

export interface WithPublicationPowerUserParentProps {
  publicationOid: string;
  userOid: string;
}

const queryName = 'publicationPowerUserData';

type Props = NamedProps<
    {[queryName]: GraphqlQueryControls & PublicationPowerUserQuery},
    WithPublicationPowerUserParentProps
  >;

export const withPublicationPowerUser =
  graphql<
    WithPublicationPowerUserParentProps,
    PublicationPowerUserQuery,
    {},
    WithPublicationPowerUserProps
  >(publicationPowerUserQuery, {
    name: queryName,
    options: ({publicationOid, userOid}) => ({
      variables: {
        publicationOid,
        userOid
      }
    }),
    props: ({ publicationPowerUserData }: Props): WithPublicationPowerUserProps => {
      const { loading } = publicationPowerUserData;
      const publicationPowerUser = publicationPowerUserData.getPublicationPowerUser;

      return { loading, publicationPowerUser };
    }
  });
