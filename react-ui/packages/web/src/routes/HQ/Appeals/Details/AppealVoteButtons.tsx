import * as React from 'react';
import { compose, withHandlers, withProps } from 'recompose';
import { Col, Row } from 'antd';
import {
  TribunalIssue,
  VoteOnReferendumInput,
  withVoteOnReferendum,
  WithVoteOnReferendumProps,
  TribunalIssueType
} from '@narrative/shared';
import styled from '../../../../shared/styled';
import { FormattedMessage } from 'react-intl';
import { AppealDetailsMessages } from '../../../../shared/i18n/AppealDetailsMessages';
import { FlexContainer } from '../../../../shared/styled/shared/containers';
import { SharedComponentMessages } from '../../../../shared/i18n/SharedComponentMessages';
import { InjectedIntlProps, injectIntl } from 'react-intl';
import { VoteButton } from '../../../../shared/components/VoteButton';
import { showValidationErrorDialogIfNecessary } from '../../../../shared/utils/webErrorUtils';

const ButtonsWrapper = styled.div`
  width: 100%;
  margin-top: 25px;
`;

interface WithHandlers {
  // tslint:disable-next-line no-any
  handleButtonClick: (isApproved: boolean) => any;
}

interface WithProps {
  activeApprovedButton: boolean;
  activeRejectedButton: boolean;
}

interface ParentProps {
  tribunalIssue: TribunalIssue;
  isTribunalMember: boolean;
}

type Props =
  ParentProps &
  WithProps &
  WithHandlers &
  WithVoteOnReferendumProps &
  InjectedIntlProps;

const AppealVoteButtonsComponent: React.SFC<Props> = (props) => {
  const { handleButtonClick, activeApprovedButton, activeRejectedButton, tribunalIssue, isTribunalMember } = props;

  // jw: if this is not a tribunal member or the referendum has closed, let's short out.
  if (!isTribunalMember || !tribunalIssue.referendum.open) {
    return null;
  }

  const forRatifyPublication = TribunalIssueType.RATIFY_PUBLICATION === tribunalIssue.type;
  const forRatifyNiche = TribunalIssueType.RATIFY_NICHE === tribunalIssue.type;

  const approveButtonMessage = forRatifyPublication
    ? AppealDetailsMessages.KeepPublication
    : forRatifyNiche
    ? AppealDetailsMessages.KeepNiche
    : AppealDetailsMessages.ApproveNiche;
  const rejectButtonMessage = forRatifyPublication
    ? AppealDetailsMessages.RejectPublication
    : forRatifyNiche
    ? AppealDetailsMessages.RejectNiche
    : AppealDetailsMessages.KeepNicheRejected;

  return (
    <ButtonsWrapper>
      <Row gutter={16}>
        <Col span={12}>
          <FlexContainer centerAll={true}>
            <VoteButton
              buttonType="primary"
              isButtonActive={activeApprovedButton}
              onClick={() => handleButtonClick(true)}
            >
              <FormattedMessage {...approveButtonMessage}/>
            </VoteButton>
          </FlexContainer>
        </Col>

        <Col span={12}>
          <FlexContainer centerAll={true}>
            <VoteButton
              buttonType="danger"
              isButtonActive={activeRejectedButton}
              onClick={() => handleButtonClick(false)}
            >
              <FormattedMessage {...rejectButtonMessage}/>
            </VoteButton>
          </FlexContainer>
        </Col>
      </Row>
    </ButtonsWrapper>
  );
};

export const AppealVoteButtons = compose(
  withProps((props: ParentProps) => {
    const { tribunalIssue: { referendum: { currentUserVote } }} = props;

    const votedFor = currentUserVote && currentUserVote.votedFor;
    const activeApprovedButton = !!(votedFor);
    const activeRejectedButton = votedFor === false;

    return { activeApprovedButton, activeRejectedButton };
  }),
  withVoteOnReferendum,
  injectIntl,
  withHandlers({
    handleButtonClick: (props: Props) => async (isApproved: boolean) => {
      const { activeApprovedButton, activeRejectedButton, intl, tribunalIssue: { referendum } } = props;

      if (isApproved) {
        if (activeApprovedButton) {
          return;
        }

      } else if (activeRejectedButton) {
        return;
      }

      const input: VoteOnReferendumInput = { votedFor: isApproved };

      try {
        await props.voteOnReferendum(input, referendum.oid);
      } catch (err) {
        showValidationErrorDialogIfNecessary(intl.formatMessage(SharedComponentMessages.FormErrorTitle), err);
      }
    }
  })
)(AppealVoteButtonsComponent) as React.ComponentClass<ParentProps>;
