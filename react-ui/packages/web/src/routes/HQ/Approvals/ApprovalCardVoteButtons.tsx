import * as React from 'react';
import { compose, withHandlers, withProps } from 'recompose';
import { Col, Row } from 'antd';
import { InjectedIntlProps, injectIntl } from 'react-intl';
import { ApprovalCardVoteButton } from './ApprovalCardVoteButton';
import { ModalConnect, ModalName, ModalStoreProps } from '../../../shared/stores/ModalStore';
import { EnhancedReferendumType } from '../../../shared/enhancedEnums/referendumType';
import { showValidationErrorDialogIfNecessary } from '../../../shared/utils/webErrorUtils';
import { SharedComponentMessages } from '../../../shared/i18n/SharedComponentMessages';
import { RevokeReasonMessages } from '../../../shared/i18n/RevokeReasonMessages';
import {
  WithPermissionsModalControllerProps,
  withPermissionsModalController
} from '../../../shared/containers/withPermissionsModalController';
import {
  Referendum,
  VoteOnReferendumInput,
  withState,
  WithStateProps,
  withVoteOnReferendum,
  WithVoteOnReferendumProps
} from '@narrative/shared';
import styled from '../../../shared/styled';
import { PermissionErrorModal } from '../../../shared/components/PermissionErrorModal';

const ButtonsWrapper = styled.div`
  width: 100%;
  margin-top: auto;
`;

// tslint:disable no-any
interface WithHandlers {
  handleButtonClick: (isApproved: boolean) => any;
}

interface WithProps {
  activeApprovedButton: boolean;
  activeRejectedButton: boolean;
  hasPreviouslyVoted: boolean;
}

interface State {
  isVoting: boolean;
}

const initialState: State = {
  isVoting: false
};

interface ParentProps {
  referendum: Referendum;
  showRejectReasonForm: () => any;
}
// tslint:enable no-any

type Props =
  ParentProps &
  WithStateProps<State> &
  WithPermissionsModalControllerProps &
  WithProps &
  WithHandlers &
  ModalStoreProps &
  WithVoteOnReferendumProps &
  InjectedIntlProps;

export const ApprovalCardVoteButtonsComponent: React.SFC<Props> = (props) => {
  const {
    referendum,
    handleButtonClick,
    activeApprovedButton,
    activeRejectedButton,
    permissionErrorModalProps,
    state
  } = props;

  // jw: if the referendum is not open, then do not include the vote buttons
  if (!referendum.open) {
    return null;
  }

  return (
    <ButtonsWrapper>
      <Row gutter={16}>
        <Col span={12}>
          <ApprovalCardVoteButton
            iconType="like"
            buttonType="primary"
            isVoting={state.isVoting}
            isButtonActive={activeApprovedButton}
            onClick={() => handleButtonClick(true)}
          />
        </Col>

        <Col span={12}>
          <ApprovalCardVoteButton
            iconType="dislike"
            buttonType="danger"
            isDisabled={state.isVoting}
            isButtonActive={activeRejectedButton}
            onClick={() => handleButtonClick(false)}
          />
        </Col>
      </Row>
      {permissionErrorModalProps && <PermissionErrorModal {...permissionErrorModalProps}/>}
    </ButtonsWrapper>
  );
};

export const ApprovalCardVoteButtons = compose(
  withPermissionsModalController('voteOnApprovals', RevokeReasonMessages.VoteOnApprovals),
  ModalConnect(ModalName.login),
  injectIntl,
  withState<State>(initialState),
  withProps((props: Props) => {
    const { userAuthenticated, referendum: { currentUserVote } } = props;

    const hasPreviouslyVoted = !!currentUserVote;
    const votedFor = currentUserVote && currentUserVote.votedFor;
    const activeApprovedButton = !!(userAuthenticated && votedFor);
    const activeRejectedButton = !!(userAuthenticated && votedFor === false);
    return { activeApprovedButton, activeRejectedButton, hasPreviouslyVoted };
  }),
  withVoteOnReferendum,
  withHandlers({
    handleButtonClick: (props: Props) => async (isApproved: boolean) => {
      const {
        referendum,
        userAuthenticated,
        modalStoreActions,
        showRejectReasonForm,
        activeApprovedButton,
        hasPreviouslyVoted,
        intl,
        granted,
        handleShowPermissionsModal,
        setState,
        state: {isVoting}
      } = props;

      // jw: if the user is not authenticated let's prompt them to sign in. No need to move any further.
      if (!userAuthenticated) {
        modalStoreActions.updateModalVisibility(ModalName.login);
        return;
      }

      // jw: if the vote already matches what they selected, short out!
      if (hasPreviouslyVoted && activeApprovedButton === isApproved) {
        return;
      }

      if (!granted) {
        handleShowPermissionsModal();
        return;
      }

      // jw: if we are voting against this niche, and this referendum type requires a reason for negative votes,
      //     then let's flip the card and require the user to specify a reason!
      if (!isApproved && EnhancedReferendumType.get(referendum.type).isRequiresRejectionReason()) {
        showRejectReasonForm();
        return;
      }

      // zb: ignore the click if the server is already processing the vote
      if (isVoting) {
        return;
      }

      setState(ss => ({...ss, isVoting: true}));

      // jw: finally, let's submit their vote to the server!
      try {
        const input: VoteOnReferendumInput = { votedFor: isApproved };

        await props.voteOnReferendum(input, referendum.oid);
      } catch (err) {
        showValidationErrorDialogIfNecessary(intl.formatMessage(SharedComponentMessages.FormErrorTitle), err);
      } finally {
        setState(ss => ({...ss, isVoting: false}));
      }

    },
  })
)(ApprovalCardVoteButtonsComponent) as React.ComponentClass<ParentProps>;
