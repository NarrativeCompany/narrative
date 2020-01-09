import * as React from 'react';
import { ReferendumVote } from '@narrative/shared';
import styled from '../../styled';
import { FlexContainer } from '../../styled/shared/containers';
import { MemberLink } from '../user/MemberLink';
import { ReferendumVoteReasonIcon } from './ReferendumVoteReasonIcon';
import { MemberAvatar } from '../user/MemberAvatar';

const VoteMemberDisplayName = styled.span`
  margin-left: 6px;
`;

interface PointsContainerProps {
  vote: ReferendumVote;
}

const VotePoints = styled.span<PointsContainerProps>`
  margin-left: 6px;
  color: ${p => p.theme.greyBlue};
`;

interface Props {
  vote: ReferendumVote;
  includePoints?: boolean;
}

export const ReferendumVoter: React.SFC<Props> = (props) => {
  const { vote, vote: { voter }, includePoints } = props;

  return (
    <FlexContainer centerAll={true}>
      <MemberAvatar user={voter} />

      <VoteMemberDisplayName>
        <MemberLink user={voter} hideBadge={true}/>
      </VoteMemberDisplayName>

      {includePoints && <VotePoints vote={vote}>({vote.votePoints})</VotePoints>}

      <ReferendumVoteReasonIcon vote={vote}/>
    </FlexContainer>
  );
};
