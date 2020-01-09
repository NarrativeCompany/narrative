import * as React from 'react';
import { compose, withProps } from 'recompose';
import { ReferendumProps } from '../../../routes/HQ/Approvals/Details/ApprovalDetails';
import { ReferendumVote, withReferendumVotesByType, WithReferendumVotesByTypeProps } from '@narrative/shared';
import { withLoadingPlaceholder } from '../../utils/withLoadingPlaceholder';
import { ReferendumVotesList } from './ReferendumVotesList';
import { EnhancedReferendumType } from '../../enhancedEnums/referendumType';

export interface VotesListLoadMoreProps extends ReferendumProps {
  votedFor?: boolean | null;
  lastVoterDisplayName?: string | null;
  lastVoterUsername?: string | null;
}

interface Props extends VotesListLoadMoreProps {
  hasMoreItems?: boolean;
  items: ReferendumVote[] | null;
}

const ReferendumVotesListLoadMoreComponent: React.SFC<Props> = (props) => {
  const { referendum, items, hasMoreItems, votedFor, lastVoterDisplayName, lastVoterUsername } = props;
  const newProps = { referendum, items, hasMoreItems, votedFor, lastVoterDisplayName, lastVoterUsername };

  // jw: unfortunately, this propery is too long to include the newProps above, so let's just add it separately.
  const enhancedReferendumType = EnhancedReferendumType.get(referendum.type);
  const includePoints = !enhancedReferendumType.isTribunalType();

  return (
    <ReferendumVotesList includePoints={includePoints} {...newProps} />
  );
};

export const ReferendumVotesListLoadMore = compose(
  withProps((props: VotesListLoadMoreProps) => {
    const referendumOid = props.referendum && props.referendum.oid;

    return { referendumOid };
  }),
  withReferendumVotesByType,
  withProps((props: WithReferendumVotesByTypeProps) => {
    const { referendumVotesData } = props;
    const { getReferendumVotesByType, loading } = referendumVotesData;

    // jw: let's spread the ReferendumVoteGrouping over the outgoing properties so they just become root level.
    return { loading, ...getReferendumVotesByType };
  }),
  withLoadingPlaceholder()
)(ReferendumVotesListLoadMoreComponent) as React.ComponentClass<VotesListLoadMoreProps>;
