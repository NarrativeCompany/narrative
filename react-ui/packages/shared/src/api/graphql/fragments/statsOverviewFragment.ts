import gql from 'graphql-tag';

import { TopNicheFragment } from './topNicheFragment';
import { NrveUsdValueFragment } from './nrveUsdValueFragment';

export const StatsOverviewFragment = gql`
  fragment StatsOverview on StatsOverview {
    totalMembers
    uniqueVisitorsPast30Days
    nicheOwners
    approvedNiches
    activeNiches
    networkRewardsPaidLastMonth @type(name: "NrveUsdValue") {
      ...NrveUsdValue
    }
    allTimeReferralRewards @type(name: "NrveUsdValue") {
      ...NrveUsdValue
    }
    totalPosts
    topNiches @type(name: "TopNiche") {
      ...TopNiche
    }
    timestamp
  }
  ${TopNicheFragment}
  ${NrveUsdValueFragment}
`;
