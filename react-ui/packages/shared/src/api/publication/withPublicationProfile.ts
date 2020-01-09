import { graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { publicationProfileQuery } from '../graphql/publication/publicationProfileQuery';
import { PublicationProfile, PublicationProfileQuery } from '../../types';
import { LoadingProps } from '../../utils';

// jw: per new standards, let's trim the fat on what we are exposing and limit it to just what we care about:
export interface WithPublicationProfileProps extends LoadingProps {
  publicationProfile: PublicationProfile;
}

export interface WithPublicationProfileParentProps {
  publicationOid: string;
}

const queryName = 'publicationProfileData';

type Props = NamedProps<
  {[queryName]: GraphqlQueryControls & PublicationProfileQuery},
  WithPublicationProfileParentProps
>;

export const withPublicationProfile =
  graphql<
    WithPublicationProfileParentProps,
    PublicationProfileQuery,
    {},
    WithPublicationProfileProps
  >(publicationProfileQuery, {
    name: queryName,
    options: ({publicationOid}) => ({
      variables: {
        publicationOid
      }
    }),
    props: ({ publicationProfileData }: Props): WithPublicationProfileProps => {
      const { loading } = publicationProfileData;
      const publicationProfile = publicationProfileData.getPublicationProfile;

      return { loading, publicationProfile };
    }
  });
