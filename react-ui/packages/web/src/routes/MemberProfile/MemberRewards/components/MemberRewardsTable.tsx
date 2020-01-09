import { RewardsRow, RewardsWrapper } from '../../../HQ/Reporting/Rewards/RewardsRow';
import { FormattedMessage } from 'react-intl';
import { RewardsMessages } from '../../../../shared/i18n/RewardsMessages';
import { UsdAndNrveValue } from '../../../../shared/components/rewards/UsdAndNrveValue';
import * as React from 'react';
import { compose, withProps } from 'recompose';
import { User, WithExtractedUserRewardPeriodRewardsProps, withUserRewardPeriodRewards } from '@narrative/shared';
import { withLoadingPlaceholder } from '../../../../shared/utils/withLoadingPlaceholder';
import { MemberRewardsMessages } from '../../../../shared/i18n/MemberRewardsMessages';
import { themeColors } from '../../../../shared/styled/theme';
import { Link } from '../../../../shared/components/Link';
import { createUrl } from '../../../../shared/utils/routeUtils';
import { WebRoute } from '../../../../shared/constants/routes';
import { monthParam as hqRewardsMonthParam } from '../../../HQ/Reporting/Rewards/Rewards';
import { NrveValue } from '../../../../shared/components/rewards/NrveValue';
import { Text } from '../../../../shared/components/Text';
import { monthParam as nicheRewardsMonthParam } from '../../../Niche/Details/Rewards/components/NicheRewardsOverview';
import { getChannelUrl } from '../../../../shared/utils/channelUtils';

interface ParentProps {
  user: User;
  month: string;
}

type Props = ParentProps & WithExtractedUserRewardPeriodRewardsProps;

const MemberRewardsTableComponent: React.SFC<Props> = (props) => {
  const { user, month, rewardPeriodStats } = props;

  const bonusPercentage = rewardPeriodStats.activityBonusPercentage;
  // bl: a little bit of a hack, but we can identify Founders by the existence of the 'Founding Member' label
  const labels: string[] = user.labels || [];
  const isFounder = labels.find(label => label === 'Founding Member');

  const earningsPercentage = rewardPeriodStats.percentageOfTotalPayout;

  const totalPayout = (
    <Link to={createUrl(WebRoute.NetworkStatsRewards, {[hqRewardsMonthParam]: month})} size="inherit">
      <FormattedMessage {...MemberRewardsMessages.TotalNarrativeRewardsPayout}/>
    </Link>
  );

  return (
    <RewardsWrapper style={{marginBottom: 30}}>
      <RewardsRow
        title={<FormattedMessage {...RewardsMessages.RewardPeriod}/>}
        value={rewardPeriodStats.rewardPeriodRange}
        style={{fontSize: 22}}
      />
      <RewardsRow
        title={<FormattedMessage {...MemberRewardsMessages.ContentCreation}/>}
        value={<UsdAndNrveValue nrveUsdValue={rewardPeriodStats.totalContentCreationReward}/>}
      />
      <RewardsRow
        title={<FormattedMessage {...MemberRewardsMessages.NicheOwnership}/>}
        value={<UsdAndNrveValue nrveUsdValue={rewardPeriodStats.totalNicheOwnershipReward}/>}
      />
      {rewardPeriodStats.nicheOwnershipRewards.map(nicheOwnershipReward => {
        const { niche, reward } = nicheOwnershipReward;

        const nicheRewardsUrl = createUrl(
          getChannelUrl(niche, WebRoute.NicheRewards),
          { [nicheRewardsMonthParam]: month }
        );

        const title = (
          <React.Fragment>
            <Link to={nicheRewardsUrl} style={{marginRight: 10}} target="_blank">
              {niche.name}
            </Link>
            <Text size="large" style={{color: themeColors.lightGray}}>
              <NrveValue value={reward}/>
            </Text>
          </React.Fragment>
        );

        return (
          <RewardsRow
            key={niche.oid}
            title={title}
            value={''}
            style={{marginLeft: 25, fontSize: 16}}
          />
        );
      })}

      <RewardsRow
        title={<FormattedMessage {...MemberRewardsMessages.NicheModeration}/>}
        value={<UsdAndNrveValue nrveUsdValue={rewardPeriodStats.totalNicheModerationReward}/>}
      />
      <RewardsRow
        title={<FormattedMessage {...MemberRewardsMessages.ActivityRewards}/>}
        value={<UsdAndNrveValue nrveUsdValue={rewardPeriodStats.totalActivityRewards}/>}
      />
      {bonusPercentage > 0 &&
        <RewardsRow
          title={<FormattedMessage {...MemberRewardsMessages.ActivityRewardsHighRepBonus} values={{bonusPercentage}}/>}
          value={''}
          style={{marginLeft: 25, color: themeColors.lightGray, fontSize: 14}}
        />
      }
      {isFounder &&
        <RewardsRow
          title={<FormattedMessage {...MemberRewardsMessages.ActivityRewardsFounderBonus}/>}
          value={''}
          style={{marginLeft: 25, color: themeColors.lightGray, fontSize: 14}}
        />
      }
      <RewardsRow
        title={<FormattedMessage {...MemberRewardsMessages.Tribunal}/>}
        value={<UsdAndNrveValue nrveUsdValue={rewardPeriodStats.totalTribunalReward}/>}
      />
      <RewardsRow
        title={<FormattedMessage {...MemberRewardsMessages.TotalRewardPointsEarned}/>}
        value={<UsdAndNrveValue nrveUsdValue={rewardPeriodStats.totalReward}/>}
        style={{fontSize: 22, fontWeight: 600}}
      />
      <RewardsRow
        title={<FormattedMessage
          {...MemberRewardsMessages.EarningsPercentageOfTotal}
          values={{earningsPercentage, totalPayout}}
        />}
        value={''}
        style={{color: themeColors.lightGray, fontSize: 16}}
      />
    </RewardsWrapper>
  );
};

export const MemberRewardsTable = compose(
  withProps((props: ParentProps) => {
    const userOid = props.user.oid;
    return { userOid };
  }),
  withUserRewardPeriodRewards,
  withLoadingPlaceholder()
)(MemberRewardsTableComponent) as React.ComponentClass<ParentProps>;
