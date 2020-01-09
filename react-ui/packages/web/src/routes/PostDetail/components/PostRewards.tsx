import * as React from 'react';
import { FormattedMessage } from 'react-intl';
import { SectionHeader } from '../../../shared/components/SectionHeader';
import { PostDetailMessages } from '../../../shared/i18n/PostDetailMessages';
import { withAllTimePostRewards, WithExtractedAllTimePostRewardsProps } from '@narrative/shared';
import { compose } from 'recompose';
import { ContainedLoading } from '../../../shared/components/Loading';
import { PostPendingRewards } from './PostPendingRewards';
import { PostRewardsValue } from './PostRewardsValue';
import { Block } from '../../../shared/components/Block';

interface ParentProps {
  postOid: string;
}

const PostRewardsComponent: React.SFC<WithExtractedAllTimePostRewardsProps> = (props) => {
  const { loading, allTimePostRewards } = props;

  return (
    <Block style={{marginBottom: 20}}>
      <SectionHeader
        title={<FormattedMessage {...PostDetailMessages.ThisPostsRewardsTitle}/>}
        style={{marginBottom: 10}}
      />

      {loading ?
        <ContainedLoading />
        : (allTimePostRewards.nrve === '0' ?
          <PostPendingRewards/> :
          <PostRewardsValue allTimePostRewards={allTimePostRewards}/>
        )
      }
    </Block>
  );
};

export const PostRewards = compose(
  withAllTimePostRewards
)(PostRewardsComponent) as React.ComponentClass<ParentProps>;
