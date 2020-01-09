import * as React from 'react';
import { WithRewardPeriodStatsProps } from './RewardsBody';
import { Heading } from '../../../../shared/components/Heading';
import { HighlightedCard } from '../../../../shared/components/HighlightedCard';
import { FormattedMessage } from 'react-intl';
import { RewardsMessages } from '../../../../shared/i18n/RewardsMessages';
import { RewardsRow, RewardsWrapper } from './RewardsRow';
import { UsdAndNrveValue } from '../../../../shared/components/rewards/UsdAndNrveValue';
import { Paragraph } from '../../../../shared/components/Paragraph';

export const SourceOfRewardsFundTable: React.SFC<WithRewardPeriodStatsProps> = (props) => {
  const { rewardPeriodStats } = props;

  return (
    <React.Fragment>
      <HighlightedCard
        highlightSide="top"
        highlightWidth="wide"
        style={{marginBottom: '10px'}}
        highlightColor="gold"
      >
        <Heading size={3} weight="normal">
          <FormattedMessage {...RewardsMessages.SourceOfRewardsFund}/>
        </Heading>
        <RewardsWrapper>
          <RewardsRow
            title={<FormattedMessage {...RewardsMessages.NicheOwnershipFees}/>}
            value={<UsdAndNrveValue nrveUsdValue={rewardPeriodStats.nicheOwnershipFeeRevenue}/>}
            style={{fontSize: 16}}
          />
          {rewardPeriodStats.tokenMintRevenue.nrve !== '0' &&
          <RewardsRow
            title={<FormattedMessage {...RewardsMessages.TokenMint}/>}
            value={<UsdAndNrveValue nrveUsdValue={rewardPeriodStats.tokenMintRevenue}/>}
            style={{fontSize: 16}}
          />}
          {rewardPeriodStats.miscellaneousRevenue.nrve !== '0' &&
          <RewardsRow
            title={<FormattedMessage {...RewardsMessages.Miscellaneous}/>}
            value={<UsdAndNrveValue nrveUsdValue={rewardPeriodStats.miscellaneousRevenue}/>}
            style={{fontSize: 16}}
          />}
          {rewardPeriodStats.carryoverRevenue.nrve !== '0' &&
          <RewardsRow
            title={<FormattedMessage {...RewardsMessages.CarryoverRewards}/>}
            value={<UsdAndNrveValue nrveUsdValue={rewardPeriodStats.carryoverRevenue}/>}
            style={{fontSize: 16}}
          />}
          <RewardsRow
            title={<FormattedMessage {...RewardsMessages.TotalFunds}/>}
            value={<UsdAndNrveValue nrveUsdValue={rewardPeriodStats.totalRevenue}/>}
          />
        </RewardsWrapper>
      </HighlightedCard>
      <Paragraph size="large" color="lightGray">
        <FormattedMessage {...RewardsMessages.MiscellaneousFootnote}/>
      </Paragraph>
    </React.Fragment>
  );
};
