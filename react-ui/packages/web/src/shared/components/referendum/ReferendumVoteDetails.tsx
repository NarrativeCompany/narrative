import * as React from 'react';
import { Col, Row } from 'antd';
import { FlexContainer, FlexContainerProps } from '../../styled/shared/containers';
import { Heading } from '../Heading';
import styled from '../../styled';
import { ReferendumType, ReferendumVotes, withReferendumVotes, WithReferendumVotesProps } from '@narrative/shared';
import { compose, withProps } from 'recompose';
import { FormattedMessage } from 'react-intl';
import { ReferendumProgressBar } from './ReferendumProgressBar';
import { cardPlaceholder, withLoadingPlaceholder } from '../../utils/withLoadingPlaceholder';
import { ReferendumProps } from '../../../routes/HQ/Approvals/Details/ApprovalDetails';
import { ReferendumMessages } from '../../i18n/ReferendumMessages';
import { ReferendumVotesList } from './ReferendumVotesList';
import { SectionHeader } from '../SectionHeader';
import { LocalizedNumber } from '../LocalizedNumber';
import { ReferendumVoteColumnHeading } from './ReferendumVoteColumnHeading';

const VoteDetailWrapper = styled<FlexContainerProps>(FlexContainer)`
  padding: 12px 0;
`;

interface Props extends ReferendumProps {
  referendumVotes: ReferendumVotes;
}

const ReferendumVoteDetailsComponent: React.SFC<Props> = (props) => {
  const { referendum, referendumVotes } = props;

  if (!referendumVotes) {
    // todo:error-handling: We should probably log with the server that we failed to get vote data for a referendum.
    //      Even if not votes have taken place, we should have at least gotten data back.
    return null;
  }

  const membersYetToVote = referendumVotes &&
    referendumVotes.tribunalMembersYetToVote &&
    referendumVotes.tribunalMembersYetToVote.items || [];

  let votedForHeading;
  let votedAgainstHeading;
  let yetToVoteHeading;
  let columnSize;
  switch (referendum.type) {
    case ReferendumType.TRIBUNAL_APPROVE_NICHE_DETAIL_CHANGE:
      votedForHeading = (
        <ReferendumVoteColumnHeading
          title={ReferendumMessages.VotedToApproveEdit}
          pointsMessage={ReferendumMessages.Votes}
          formattedPoints={<LocalizedNumber value={parseFloat(referendum.votePointsFor)}/>}
        />);
      votedAgainstHeading = (
        <ReferendumVoteColumnHeading
          title={ReferendumMessages.VotedToRejectEdit}
          pointsMessage={ReferendumMessages.Votes}
          formattedPoints={<LocalizedNumber value={parseFloat(referendum.votePointsAgainst)}/>}
        />);
      yetToVoteHeading = (
        <ReferendumVoteColumnHeading
          title={ReferendumMessages.YetToVote}
          pointsMessage={ReferendumMessages.TribunalMembers}
          formattedPoints={<LocalizedNumber value={membersYetToVote.length}/>}
        />);

      columnSize = 8;
      break;
    case ReferendumType.TRIBUNAL_RATIFY_NICHE:
    case ReferendumType.TRIBUNAL_APPROVE_REJECTED_NICHE:
    case ReferendumType.TRIBUNAL_RATIFY_PUBLICATION:
      votedForHeading = (
        <ReferendumVoteColumnHeading
          title={ReferendumMessages.VotedFor}
          pointsMessage={ReferendumMessages.Votes}
          formattedPoints={<LocalizedNumber value={parseFloat(referendum.votePointsFor)}/>}
        />);
      votedAgainstHeading = (
        <ReferendumVoteColumnHeading
          title={ReferendumMessages.VotedAgainst}
          pointsMessage={ReferendumMessages.Votes}
          formattedPoints={<LocalizedNumber value={parseFloat(referendum.votePointsAgainst)}/>}
        />);
      yetToVoteHeading = (
        <ReferendumVoteColumnHeading
          title={ReferendumMessages.YetToVote}
          pointsMessage={ReferendumMessages.TribunalMembers}
          formattedPoints={<LocalizedNumber value={membersYetToVote.length}/>}
        />);
      columnSize = 8;
      break;
    default:
      votedForHeading = (
        <ReferendumVoteColumnHeading
          title={ReferendumMessages.VotedFor}
          pointsMessage={ReferendumMessages.TotalPoints}
          formattedPoints={<LocalizedNumber value={parseFloat(referendum.votePointsFor)} minFractionLength={2}/>}
        />);
      votedAgainstHeading = (
        <ReferendumVoteColumnHeading
          title={ReferendumMessages.VotedAgainst}
          pointsMessage={ReferendumMessages.TotalPoints}
          formattedPoints={<LocalizedNumber value={parseFloat(referendum.votePointsAgainst)} minFractionLength={2}/>}
        />);
      yetToVoteHeading = null;
      columnSize = 12;
  }

  const forTribunal = yetToVoteHeading !== null;

  const totalVotes = referendumVotes.totalVotes;
  const totalVotesText = <LocalizedNumber value={totalVotes} />;
  const pointTotal = parseFloat(referendumVotes.votePointsFor) + parseFloat(referendumVotes.votePointsAgainst);
  const pointTotalText = <LocalizedNumber value={pointTotal} minFractionLength={yetToVoteHeading ? undefined : 2}/>;
  const title = forTribunal
    ? <FormattedMessage {...ReferendumMessages.TribunalVoteDetails} values={{ totalVotesText }}/>
    : <FormattedMessage {...ReferendumMessages.VoteTally} values={{ pointTotalText, totalVotesText, totalVotes }}/>;

  const { votePointsFor, votePointsAgainst, recentVotesFor, recentVotesAgainst } = referendumVotes;

  return (
    <VoteDetailWrapper column={true}>
      <SectionHeader title={title}/>

      <Heading size={6} uppercase={true}>
        <FormattedMessage {...ReferendumMessages.ApprovalRating}/>
      </Heading>
      <ReferendumProgressBar votePointsFor={votePointsFor} votePointsAgainst={votePointsAgainst}/>

      <Row>
        <Col md={columnSize}>
          {votedForHeading}
          <ReferendumVotesList referendum={referendum} includePoints={!forTribunal} {...recentVotesFor} />
        </Col>
        <Col md={columnSize}>
          {votedAgainstHeading}
          <ReferendumVotesList referendum={referendum} includePoints={!forTribunal} {...recentVotesAgainst} />
        </Col>
        {yetToVoteHeading &&
        <Col md={columnSize}>
          {yetToVoteHeading}
          <ReferendumVotesList referendum={referendum} items={membersYetToVote}/>
        </Col>}
      </Row>
    </VoteDetailWrapper>
  );
};

export const ReferendumVoteDetails = compose(
  withProps((props: ReferendumProps) => {
    const { referendum } = props;
    const referendumOid = referendum.oid;

    return { referendumOid, uniqueQueryStrValue: new Date().getTime() };
  }),
  withReferendumVotes,
  withProps((props: WithReferendumVotesProps) => {
    const { referendumVotesData } = props;
    const { getReferendumVotes, loading } = referendumVotesData;

    return { loading, referendumVotes: getReferendumVotes };
  }),
  withLoadingPlaceholder(cardPlaceholder)
)(ReferendumVoteDetailsComponent) as React.ComponentClass<ReferendumProps>;
