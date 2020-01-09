import { graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { statsOverviewQuery } from '../graphql/stats/statsOverviewQuery';
import { StatsOverview, StatsOverviewQuery } from '../../types';
import { LoadingProps } from '../../utils';

const queryName = 'statsOverviewData';

export interface WithExtractedStatsOverviewProps  extends LoadingProps {
  statsOverview: StatsOverview;
}

export type WithStatsOverviewProps = NamedProps<{[queryName]: GraphqlQueryControls & StatsOverviewQuery}, {}>;

export const withStatsOverview =
  graphql<{}, StatsOverviewQuery, {}, WithExtractedStatsOverviewProps>(statsOverviewQuery, {
    name: queryName,
    props: ({ statsOverviewData }: WithStatsOverviewProps) => {
      const { loading } = statsOverviewData;
      const statsOverview = statsOverviewData.getStatsOverview;

      return { loading, statsOverview };
    }
  });
