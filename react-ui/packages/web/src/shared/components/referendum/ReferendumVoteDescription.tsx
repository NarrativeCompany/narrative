import * as React from 'react';
import { Referendum } from '@narrative/shared';
import { FormattedMessage } from 'react-intl';
import { ReferendumMessages } from '../../i18n/ReferendumMessages';
import styled from '../../styled';
import { EnhancedReferendumType } from '../../enhancedEnums/referendumType';

interface ActionTextProps {
  votedFor: boolean;
}

const ActionText = styled.span<ActionTextProps>`
  color: ${props => props.votedFor ? props.theme.primaryBlue : props.theme.secondaryRed};
`;

interface Props {
  referendum: Referendum;
}

export const ReferendumVoteDescription: React.SFC<Props> = (props) => {
  const { referendum, referendum: { currentUserVote } } = props;

  // jw: if there is no current vote, then let's
  if (!currentUserVote) {
    if (referendum.open) {
      return <FormattedMessage {...ReferendumMessages.YouNeedToVoteText}/>;
    } else {
      return <FormattedMessage {...ReferendumMessages.YouDidNotVoteText}/>;
    }
  }

  const enhancedType = EnhancedReferendumType.get(referendum.type);

  const actionText = (
    <ActionText votedFor={currentUserVote.votedFor}>
      <FormattedMessage {...enhancedType.getVoteActionMessage(currentUserVote)} />
    </ActionText>
  );

  return <FormattedMessage {...enhancedType.getVotedMessage()} values={{actionText}} />;
};
