import { TribunalAppealCardYourVoteMessage } from './TribunalAppealCardYourVoteMessage';
import {
  TribunalIssue,
  TribunalIssueStatus
} from '@narrative/shared';
import { TribunalAppealCardGoToVoteButton } from './TribunalAppealCardGoToVoteButton';
import * as React from 'react';

interface Props {
  appeal: TribunalIssue;
}

export const TribunalAppealCardYourVoteInfo: React.SFC<Props> = (props) => {
  const { appeal, appeal: { referendum } } = props;

  if (referendum.currentUserVote) {
    // you have voted
    return (
      <TribunalAppealCardYourVoteMessage appeal={appeal} />
    );
  }

  if (TribunalIssueStatus.OPEN === status) {
    // you need to vote
    return <TribunalAppealCardGoToVoteButton tribunalIssueOid={appeal.oid} type={appeal.type}/>;
  }

  // nothing shown
  return null;
};
