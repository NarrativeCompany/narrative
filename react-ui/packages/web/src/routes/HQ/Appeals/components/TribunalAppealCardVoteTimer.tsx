import * as React from 'react';
import { FormattedMessage } from 'react-intl';
import { TribunalAppealCardMessages } from '../../../../shared/i18n/TribunalAppealCardMessages';
import {
  TribunalIssueStatus,
  TribunalIssue
} from '@narrative/shared';
import { TribunalAppealTimeRemaining } from './TribunalAppealTimeRemaining';
import styled from '../../../../shared/styled';
import { FlexContainer, FlexContainerProps } from '../../../../shared/styled/shared/containers';

const VoteEndedMessageWrapper = styled<FlexContainerProps>(FlexContainer)`
  margin-right: 6px;
`;

interface ParentProps {
  tribunalIssue: TribunalIssue;
}

export const TribunalAppealCardVoteTimer: React.SFC<ParentProps> = (props) => {
  const { tribunalIssue, tribunalIssue: { referendum } } = props;
  const { endDatetime } = referendum;

  if (TribunalIssueStatus.OPEN === tribunalIssue.status && referendum.open) {
    return <TribunalAppealTimeRemaining endTime={endDatetime}/>;
  }

  return (
    <VoteEndedMessageWrapper centerAll={true}>
      <FormattedMessage {...TribunalAppealCardMessages.TribunalVoteEnded}/>
    </VoteEndedMessageWrapper>
  );
};
