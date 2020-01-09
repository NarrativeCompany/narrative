import { graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { publicationDetailQuery } from '../graphql/publication/publicationDetailQuery';
import { PublicationDetail, PublicationDetailQuery } from '../../types';
import { infiniteLoadingFixProps, LoadingProps } from '../../utils';

// jw: per new standards, let's trim the fat on what we are exposing and limit it to just what we care about:
export interface WithPublicationDetailProps extends LoadingProps {
  publicationDetail: PublicationDetail;
  refetchPublicationDetail: () => void;
}

export interface WithPublicationDetailParentProps {
  publicationId: string;
}

const queryName = 'publicationDetailData';

type Props = NamedProps<{[queryName]: GraphqlQueryControls & PublicationDetailQuery}, WithPublicationDetailParentProps>;

export const withPublicationDetail =
  graphql<
    WithPublicationDetailParentProps,
    PublicationDetailQuery,
    {},
    WithPublicationDetailProps
  >(publicationDetailQuery, {
    name: queryName,
    options: ({publicationId}) => ({
      // jw: due to a redirect from publication settings when the publication name changes we are now seeing this query
      //     falling victim to the infinite loading issue during the redirect. This solves it, like it does everywhere
      //     else...
      ...infiniteLoadingFixProps,
      variables: {
        publicationId
      }
    }),
    props: ({ publicationDetailData }: Props): WithPublicationDetailProps => {
      const { loading, refetch, variables } = publicationDetailData;
      const publicationDetail = publicationDetailData.getPublicationDetail;

      // jw: let's provide a way for consumers of the PublicationDetails to refetch the details.
      const refetchPublicationDetail = () => {
        refetch(variables);
      };

      return { loading, publicationDetail, refetchPublicationDetail };
    }
  });
