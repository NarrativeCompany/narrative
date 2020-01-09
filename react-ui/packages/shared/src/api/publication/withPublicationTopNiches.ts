import { graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { publicationTopNichesQuery } from '../graphql/publication/publicationTopNichesQuery';
import { TopNiche, PublicationTopNichesQuery } from '../../types';
import { LoadingProps } from '../../utils';

// jw: per new standards, let's trim the fat on what we are exposing and limit it to just what we care about:
export interface WithPublicationTopNichesProps extends LoadingProps {
  publicationTopNiches: TopNiche[];
}

export interface WithPublicationTopNichesParentProps {
  publicationOid: string;
}

const queryName = 'publicationTopNichesData';

type Props = NamedProps<
    {[queryName]: GraphqlQueryControls & PublicationTopNichesQuery},
    WithPublicationTopNichesParentProps
  >;

export const withPublicationTopNiches =
  graphql<
    WithPublicationTopNichesParentProps,
    PublicationTopNichesQuery,
    {},
    WithPublicationTopNichesProps
  >(publicationTopNichesQuery, {
    name: queryName,
    options: ({publicationOid}) => ({
      variables: {
        publicationOid
      }
    }),
    props: ({ publicationTopNichesData }: Props): WithPublicationTopNichesProps => {
      const { loading } = publicationTopNichesData;
      const publicationTopNiches = publicationTopNichesData.getPublicationTopNiches || [];

      return { loading, publicationTopNiches };
    }
  });
