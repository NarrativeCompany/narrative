import * as React from 'react';
import { compose } from 'recompose';
import {
  NichePeriodParentProps,
  WithExtractedNicheCreatorLeaderboardProps,
  withNicheCreatorLeaderboard
} from '@narrative/shared';
import { MemberLink } from '../../../../../shared/components/user/MemberLink';
import { NicheRewardsLeaderboardCard, NicheRewardsLeaderboardCardRowProps } from './NicheRewardsLeaderboardCard';
import { FormattedMessage } from 'react-intl';
import { NicheRewardsMessages } from '../../../../../shared/i18n/NicheRewardsMessages';
import { MemberAvatar } from '../../../../../shared/components/user/MemberAvatar';

type ParentProps =
  NichePeriodParentProps;

type Props =
  ParentProps &
  WithExtractedNicheCreatorLeaderboardProps;

const NicheRewardsCreatorLeaderboardComponent: React.SFC<Props> = (props) => {
  const { loading, leaderboard, month } = props;

  const rows: NicheRewardsLeaderboardCardRowProps[] = Array.from(
    leaderboard.map(rewardLeaderboardUser => ({
      oid: rewardLeaderboardUser.user.oid,
      title: (
        <React.Fragment>
          <MemberAvatar user={rewardLeaderboardUser.user} size={16} style={{marginRight: 5}}/>
          <MemberLink user={rewardLeaderboardUser.user}/>
        </React.Fragment>
      ),
      reward: rewardLeaderboardUser.reward
    }))
  );

  return (
    <NicheRewardsLeaderboardCard
      loading={loading}
      title={!month ?
        <FormattedMessage {...NicheRewardsMessages.AllTimeTopEarningCreators}/> :
        <FormattedMessage {...NicheRewardsMessages.TopEarningCreators}/>
      }
      highlightColor="primary-blue"
      rows={rows}
    />
  );
};

export default compose(
  withNicheCreatorLeaderboard
)(NicheRewardsCreatorLeaderboardComponent) as React.ComponentClass<ParentProps>;
