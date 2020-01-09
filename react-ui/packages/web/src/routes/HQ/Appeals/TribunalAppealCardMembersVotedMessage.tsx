import * as React from 'react';
import { FormattedMessage } from 'react-intl';
import { TribunalAppealCardMembersVotedMessages } from '../../../shared/i18n/TribunalAppealCardMembersVotedMessages';
import { FlexContainer } from '../../../shared/styled/shared/containers';
import { TribunalIssueType } from '@narrative/shared';

interface ParentProps {
  votePercentage?: number;
  type: TribunalIssueType;
}

export const TribunalAppealCardMembersVotedMessage: React.SFC<ParentProps> = (props) => {
  const { votePercentage, type } = props;

  let Message;

  if (votePercentage === undefined) {
    Message = <FormattedMessage {...TribunalAppealCardMembersVotedMessages.TribunalNoVotes}/>;
  } else {

    const VOTE_THRESHOLD = 50; // got to be greater than 50%
    const voteAgainstPercentage = 100 - votePercentage;

    // should we add in different messages for when no votes have been cast?
    switch (type) {
      case TribunalIssueType.APPROVE_NICHE_DETAIL_CHANGE:
        if (votePercentage > VOTE_THRESHOLD) {
          Message = (
            <FormattedMessage
              {...TribunalAppealCardMembersVotedMessages.TribunalVotedToApproveEdit}
              values={{votePercentage}}
            />
          );
        } else {
          Message = (
            <FormattedMessage
              {...TribunalAppealCardMembersVotedMessages.TribunalVotedToRejectEdit}
              values={{voteAgainstPercentage}}
            />
          );
        }
        break;
      case TribunalIssueType.APPROVE_REJECTED_NICHE:
        if (votePercentage > VOTE_THRESHOLD) {
          Message = (
            <FormattedMessage
              {...TribunalAppealCardMembersVotedMessages.TribunalVotedToMakeNicheActive}
              values={{votePercentage}}
            />
          );
        } else {
          Message = (
            <FormattedMessage
              {...TribunalAppealCardMembersVotedMessages.TribunalVotedToKeepRejectedNiche}
              values={{voteAgainstPercentage}}
            />
          );
        }
        break;
      case TribunalIssueType.RATIFY_NICHE:
        if (votePercentage > VOTE_THRESHOLD) {
          Message = (
            <FormattedMessage
              {...TribunalAppealCardMembersVotedMessages.TribunalVotedToKeepNicheActive}
              values={{votePercentage}}
            />
          );
        } else {
          Message = (
            <FormattedMessage
              {...TribunalAppealCardMembersVotedMessages.TribunalVotedToRejectNiche}
              values={{voteAgainstPercentage}}
            />
          );
        }
        break;
      case TribunalIssueType.RATIFY_PUBLICATION:
        if (votePercentage > VOTE_THRESHOLD) {
          Message = (
            <FormattedMessage
              {...TribunalAppealCardMembersVotedMessages.TribunalVotedToKeepPublicationActive}
              values={{votePercentage}}
            />
          );
        } else {
          Message = (
            <FormattedMessage
              {...TribunalAppealCardMembersVotedMessages.TribunalVotedToRejectPublication}
              values={{voteAgainstPercentage}}
            />
          );
        }
        break;
      default:
        // should never see this
        Message = <span>{votePercentage} %</span>;
        break;
    }
  }

  return (
    <FlexContainer centerAll={true}>
      {Message}
    </FlexContainer>
  );
};
