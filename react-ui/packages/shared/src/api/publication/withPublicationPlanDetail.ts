import { graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { publicationPlanDetailQuery } from '../graphql/publication/publicationPlanDetailQuery';
import { PublicationPlanDetail, PublicationPlanDetailQuery } from '../../types';
import { LoadingProps } from '../../utils';

// jw: per new standards, let's trim the fat on what we are exposing and limit it to just what we care about:
export interface WithPublicationPlanDetailProps extends LoadingProps {
  planDetails: PublicationPlanDetail;
}

interface ParentProps {
  publicationOid: string;
}

const queryName = 'publicationPlanDetailData';

type Props = NamedProps<{[queryName]: GraphqlQueryControls & PublicationPlanDetailQuery}, ParentProps>;

export const withPublicationPlanDetail =
  graphql<
    ParentProps,
    PublicationPlanDetailQuery,
    {},
    WithPublicationPlanDetailProps
  >(publicationPlanDetailQuery, {
    name: queryName,
    options: ({publicationOid}) => ({
      variables: {
        publicationOid
      }
    }),
    props: ({ publicationPlanDetailData }: Props): WithPublicationPlanDetailProps => {
      const { loading } = publicationPlanDetailData;
      const planDetails = publicationPlanDetailData.getPublicationPlanDetail;

      return { loading, planDetails };
    }
  });
