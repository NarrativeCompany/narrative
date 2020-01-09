import * as React from 'react';
import { ReferendumVote } from '@narrative/shared';
import { List } from 'antd';
import { ReferendumVoter } from './ReferendumVoter';
import { ReferendumVotesListLoadMore, VotesListLoadMoreProps } from './ReferendumVotesListLoadMore';
import { LegacyLoadMoreButton } from '../LegacyLoadMoreButton';

interface Props extends VotesListLoadMoreProps {
  items: ReferendumVote[] | null;
  hasMoreItems?: boolean;
  includePoints?: boolean;
}

export const ReferendumVotesList: React.SFC<Props> = (props) => {
  const { items, hasMoreItems, includePoints, ...voterListFromServerProps } = props;

  if (!items || !items.length) {
    return null;
  }

  return (
    <React.Fragment>
      <List
        split={false}
        dataSource={items}
        renderItem={(vote: ReferendumVote) => (
          <List.Item key={vote.oid}>
            <ReferendumVoter vote={vote} includePoints={includePoints}/>
          </List.Item>
        )}
      />
      {hasMoreItems && <LegacyLoadMoreButton
        alignLeft={true}
        fetchMoreItems={() => {
          return <ReferendumVotesListLoadMore {...voterListFromServerProps} />;
        }}
      />}
    </React.Fragment>
  );
};
