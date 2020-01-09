import { graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { publicationSettingsQuery } from '../graphql/publication/publicationSettingsQuery';
import { PublicationSettings, PublicationSettingsQuery } from '../../types';
import { LoadingProps } from '../../utils';

// jw: per new standards, let's trim the fat on what we are exposing and limit it to just what we care about:
export interface WithPublicationSettingsProps extends LoadingProps {
  publicationSettings: PublicationSettings;
}

interface ParentProps {
  publicationOid: string;
}

const queryName = 'publicationSettingsData';

type Props = NamedProps<{[queryName]: GraphqlQueryControls & PublicationSettingsQuery}, ParentProps>;

export const withPublicationSettings =
  graphql<ParentProps, PublicationSettingsQuery, {}, WithPublicationSettingsProps>(publicationSettingsQuery, {
    name: queryName,
    options: ({publicationOid}) => ({
      variables: {
        publicationOid
      }
    }),
    props: ({ publicationSettingsData }: Props): WithPublicationSettingsProps => {
      const { loading } = publicationSettingsData;
      const publicationSettings = publicationSettingsData.getPublicationSettings;

      return { loading, publicationSettings };
    }
  });
