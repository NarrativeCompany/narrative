import * as React from 'react';
import { compose } from 'recompose';
import {
  NichePeriodParentProps,
  WithExtractedNichePostLeaderboardProps,
  withNichePostLeaderboard
} from '@narrative/shared';
import { PostLink } from '../../../../../shared/components/post/PostLink';
import { FormattedMessage } from 'react-intl';
import { NicheRewardsMessages } from '../../../../../shared/i18n/NicheRewardsMessages';
import { NicheRewardsLeaderboardCard, NicheRewardsLeaderboardCardRowProps } from './NicheRewardsLeaderboardCard';

type ParentProps =
  NichePeriodParentProps;

type Props =
  ParentProps &
  WithExtractedNichePostLeaderboardProps;

const NicheRewardsPostLeaderboardComponent: React.SFC<Props> = (props) => {
  const { loading, leaderboard, month } = props;

  const rows: NicheRewardsLeaderboardCardRowProps[] = Array.from(
    leaderboard.map(rewardLeaderboardPost => ({
      oid: rewardLeaderboardPost.postOid,
      title: rewardLeaderboardPost.post ?
        <PostLink post={rewardLeaderboardPost.post}/> :
        <FormattedMessage {...NicheRewardsMessages.DeletedPost}/>,
      reward: rewardLeaderboardPost.reward
    }))
  );

  return (
    <NicheRewardsLeaderboardCard
      loading={loading}
      title={!month ?
        <FormattedMessage {...NicheRewardsMessages.AllTimeTopEarningPosts}/> :
        <FormattedMessage {...NicheRewardsMessages.TopEarningPosts}/>
      }
      highlightColor="purple"
      rows={rows}
    />
  );
};

export default compose(
  withNichePostLeaderboard
)(NicheRewardsPostLeaderboardComponent) as React.ComponentClass<ParentProps>;
