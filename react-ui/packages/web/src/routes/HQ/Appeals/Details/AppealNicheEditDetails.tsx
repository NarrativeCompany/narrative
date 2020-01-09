import * as React from 'react';
import { FormattedMessage } from 'react-intl';
import styled from '../../../../shared/styled';
import { FlexContainer, FlexContainerProps } from '../../../../shared/styled/shared/containers';
import {
  TribunalIssue,
  TribunalIssueStatus,
  VoteOnReferendumInput,
  withVoteOnReferendum,
  WithVoteOnReferendumProps,
  mergeErrorsIntoString
} from '@narrative/shared';
import { AppealDetailsMessages } from '../../../../shared/i18n/AppealDetailsMessages';
import { compose, withHandlers, withProps } from 'recompose';
import { openNotification } from '../../../../shared/utils/notificationsUtil';
import { SharedComponentMessages } from '../../../../shared/i18n/SharedComponentMessages';
import { InjectedIntlProps, injectIntl } from 'react-intl';
import { NicheEditDetails } from '../components/NicheEditDetails';
import { VoteButton } from '../../../../shared/components/VoteButton';

const ButtonWrapper = styled<FlexContainerProps>(FlexContainer)`
  padding: 12px;
`;

interface WithHandlers {
  // tslint:disable-next-line no-any
  handleButtonClick: (isApproved: boolean) => any;
}

interface ParentProps {
  tribunalIssue: TribunalIssue;
  isTribunalMember: boolean;
}

interface WithProps {
  tribunalIssue: TribunalIssue;
  activeApprovedButton: boolean;
  activeRejectedButton: boolean;
}

type Props =
  ParentProps &
  WithProps &
  WithHandlers &
  WithVoteOnReferendumProps &
  InjectedIntlProps;

const AppealNicheEditDetailsComponent: React.SFC<Props> = (props) => {
  const { isTribunalMember, tribunalIssue } = props;
  const { referendum, nicheEditDetail } = tribunalIssue;

  if (!nicheEditDetail) {
    // todo:error-handling: We should never get here, so if we did we should log with the server so we can investigate.
    return null;
  }

  const includeVoteButtons = isTribunalMember &&
    referendum.open &&
    TribunalIssueStatus.OPEN === tribunalIssue.status;

  const {
    handleButtonClick,
    activeApprovedButton,
    activeRejectedButton,
  } = props;

  return (
    <React.Fragment>
      <NicheEditDetails
        editDetails={nicheEditDetail}

        currentVersionFooter={includeVoteButtons && <ButtonWrapper centerAll={true}>
          <VoteButton
            buttonType="danger"
            isButtonActive={activeRejectedButton}
            onClick={() => handleButtonClick(false)}
          >
            <FormattedMessage {...AppealDetailsMessages.KeepOldVersion}/>
          </VoteButton>
        </ButtonWrapper>}

        newVersionFooter={includeVoteButtons &&
          <ButtonWrapper centerAll={true}>
            <VoteButton
              buttonType="primary"
              isButtonActive={activeApprovedButton}
              onClick={() => handleButtonClick(true)}
            >
              <FormattedMessage {...AppealDetailsMessages.ApproveNewVersion}/>
            </VoteButton>
          </ButtonWrapper>}
      />
    </React.Fragment>
  );
};

export const AppealNicheEditDetails = compose(
  withProps((props: ParentProps) => {
    const { isTribunalMember } = props;

    const { currentUserVote } = props.tribunalIssue.referendum;
    const votedFor = currentUserVote && currentUserVote.votedFor;
    const activeApprovedButton = !!(isTribunalMember && votedFor);
    const activeRejectedButton = isTribunalMember && votedFor === false;

    return { activeApprovedButton, activeRejectedButton };
  }),
  withVoteOnReferendum,
  injectIntl,
  withHandlers({
    handleButtonClick: (props: Props) => async (isApproved: boolean) => {
      const { tribunalIssue, activeApprovedButton, activeRejectedButton, isTribunalMember, intl } = props;
      const { referendum } = tribunalIssue;

      if (!isTribunalMember) {
        // jw:todo:error-handling: we need to report this to the server. We should only ever get here for tribunal
        //    members.
        return;
      }

      if (isApproved) {
        if (activeApprovedButton) {
          return;
        }
      } else {
        if (activeRejectedButton) {
          return;
        }
      }

      const input: VoteOnReferendumInput = { votedFor: isApproved };

      try {
        await props.voteOnReferendum(input, referendum.oid);
      } catch (err) {
        // Collapse errors into a string and show an alert
        openNotification.updateFailed(
          err,
          {
            message: intl.formatMessage(SharedComponentMessages.FormErrorTitle),
            description: mergeErrorsIntoString(err)
          }
        );
      }
    }
  })
)(AppealNicheEditDetailsComponent) as React.ComponentClass<ParentProps>;
