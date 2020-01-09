import { ChildDataProps, graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { trendingNichesQuery } from '../graphql/niche/trendingNichesQuery';
import { Niche, TrendingNichesQuery } from '../../types';

const queryName = 'trendingNichesData';

type WithProps = NamedProps<
  {[queryName]: GraphqlQueryControls & TrendingNichesQuery},
  WithTrendingNichesProps
>;

export interface WithTrendingNichesProps extends ChildDataProps<{}, TrendingNichesQuery> {
  loadingTrendingNiches: boolean;
  trendingNiches: Niche[];
}

export const withTrendingNiches =
  graphql<
    {},
    TrendingNichesQuery,
    {},
    WithTrendingNichesProps
    >(trendingNichesQuery, {
    name: queryName,
    props: ({ trendingNichesData, ownProps }: WithProps) => {
      const loadingTrendingNiches = trendingNichesData.loading;
      const trendingNiches = extractedTrendingNiches(trendingNichesData) as Niche[];

      return { ...ownProps, loadingTrendingNiches, trendingNiches };
    }
  });

function extractedTrendingNiches (data: GraphqlQueryControls & TrendingNichesQuery) {
  return data && data.getTrendingNiches || [];
}
