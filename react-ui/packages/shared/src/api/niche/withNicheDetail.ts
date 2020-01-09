import { graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { nicheDetailQuery } from '../graphql/niche/nicheDetailQuery';
import { NicheDetail, NicheDetailQuery, NicheDetailQueryVariables } from '../../types';
import { LoadingProps } from '../../utils';

export interface WithNicheDetailProps extends LoadingProps {
  nicheDetail: NicheDetail;
}

interface ParentProps {
  nicheId: string;
}

const queryName = 'nicheDetailData';

type Props = NamedProps<{[queryName]: GraphqlQueryControls & NicheDetailQuery}, ParentProps>;

export const withNicheDetail =
  graphql<
    ParentProps,
    NicheDetailQuery,
    NicheDetailQueryVariables,
    WithNicheDetailProps
  >(nicheDetailQuery, {
    options: ({nicheId}) => ({
      variables: {
        nicheId
      }
    }),
    name: queryName,
    props: ({ nicheDetailData }: Props): WithNicheDetailProps => {
      const { loading } = nicheDetailData;
      const nicheDetail = nicheDetailData.getNicheDetail;

      return { loading, nicheDetail };
    }
  });
