import * as React from 'react';
import { FormattedMessage } from 'react-intl';
import styled from '../../../shared/styled';
import { TribunalAppealCardYourVoteMessages } from '../../../shared/i18n/TribunalAppealCardYourVoteMessages';
import { FlexContainer, FlexContainerProps } from '../../../shared/styled/shared/containers';
import {
  TribunalIssue,
  TribunalIssueStatus
} from '@narrative/shared';
import {  ReferendumVoteDescription } from '../../../shared/components/referendum/ReferendumVoteDescription';
import { TribunalIssueLink } from '../../../shared/components/tribunal/TribunalIssueLink';

const VoteMessageWrapper = styled<FlexContainerProps>(FlexContainer)`
  width: auto;
  height: 100%;
  margin-left: auto;

  @media screen and (max-width: 576px) {
    width: 100%;
    justify-content: center;
  }
`;

interface Props {
  appeal: TribunalIssue;
}

export const TribunalAppealCardYourVoteMessage: React.SFC<Props> = (props) => {
  const { appeal, appeal: { referendum } } = props;
  const { currentUserVote } = referendum;

  return (
    <VoteMessageWrapper centerAll={true}>
      <span>
        <ReferendumVoteDescription referendum={referendum} />&nbsp;
      </span>

      {TribunalIssueStatus.OPEN === status && currentUserVote &&
        <TribunalIssueLink issue={appeal} textDecoration="underline">
          <FormattedMessage {...TribunalAppealCardYourVoteMessages.ChangeVoteLinkText}/>
        </TribunalIssueLink>}
    </VoteMessageWrapper>
  );
};
