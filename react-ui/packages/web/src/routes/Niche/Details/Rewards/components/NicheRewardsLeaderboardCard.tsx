import * as React from 'react';
import { ContainedLoading } from '../../../../../shared/components/Loading';
import { HighlightColor, HighlightedCard } from '../../../../../shared/components/HighlightedCard';
import { RewardsRow, RewardsWrapper } from '../../../../HQ/Reporting/Rewards/RewardsRow';
import { Heading } from '../../../../../shared/components/Heading';
import { NrveUsdValue } from '@narrative/shared';
import { NrveValue } from '../../../../../shared/components/rewards/NrveValue';
import { UsdValue } from '../../../../../shared/components/rewards/UsdValue';

export interface NicheRewardsLeaderboardCardRowProps {
  oid: string;
  title: React.ReactNode;
  reward: NrveUsdValue;
}

export interface NicheRewardsLeaderboardCardParentProps {
  loading: boolean;
  title: React.ReactNode;
  highlightColor: HighlightColor;
  rows: NicheRewardsLeaderboardCardRowProps[];
}

export const NicheRewardsLeaderboardCard: React.SFC<NicheRewardsLeaderboardCardParentProps> = (props) => {
  const { loading, title, highlightColor, rows } = props;

  return (
    <HighlightedCard
        highlightSide="top"
        highlightWidth="wide"
        highlightColor={highlightColor}
        style={{marginBottom: 15}}
    >
      <Heading size={4} style={{textAlign: 'center'}}>
        {title}
      </Heading>

      {loading && <ContainedLoading />}
      {!loading &&
        <RewardsWrapper>
          {rows.map(row => (
            <RewardsRow
              key={row.oid}
              title={row.title}
              value={<NrveValue value={row.reward} customTooltip={<UsdValue nrveUsdValue={row.reward}/>}/>}
              style={{fontSize: 16}}
            />
          ))}
        </RewardsWrapper>
      }
    </HighlightedCard>
  );
};
