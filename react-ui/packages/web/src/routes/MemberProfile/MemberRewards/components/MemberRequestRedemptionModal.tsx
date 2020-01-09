import * as React from 'react';
import { branch, compose, renderComponent } from 'recompose';
import { withState, WithStateProps, withUserNeoWallet, WithUserNeoWalletProps } from '@narrative/shared';
import { WithLoadingPlaceholderProps } from '../../../../shared/utils/withLoadingPlaceholder';
import { EnhancedUserRedemptionStatus } from '../../../../shared/enhancedEnums/userRedemptionStatus';
import { MemberRequestRedemptionForm } from './MemberRequestRedemptionForm';
import { Link } from '../../../../shared/components/Link';
import { MemberRewardsMessages } from '../../../../shared/i18n/MemberRewardsMessages';
import { FormattedMessage } from 'react-intl';
import { MemberProfileConnect, WithMemberProfileProps } from '../../../../shared/context/MemberProfileContext';
import { WebRoute } from '../../../../shared/constants/routes';
import { generatePath, RouterProps, withRouter } from 'react-router';
import { LocalizedTime } from '../../../../shared/components/LocalizedTime';
import { MemberRewardsTransactionsChangedHandler } from '../MemberRewards';
import { Modal } from 'antd';
import { externalUrls } from '../../../../shared/constants/externalUrls';
import { Paragraph, ParagraphProps } from '../../../../shared/components/Paragraph';
import { Button } from '../../../../shared/components/Button';
import styled from 'styled-components';
import { FlexContainer, FlexContainerProps } from '../../../../shared/styled/shared/containers';
import { PointRedemptionExplanation } from './PointRedemptionExplanation';

interface ParentProps extends MemberRewardsTransactionsChangedHandler {
  dismiss: () => void;
}

interface State {
  isModalHidden?: boolean;
}

type Props = ParentProps &
  RouterProps &
  WithUserNeoWalletProps &
  WithMemberProfileProps &
  WithStateProps<State>;

const RedemptionParagraph = styled<ParagraphProps>(Paragraph)`
  margin-bottom: 20px;
`;

const RedemptionButton = styled<FlexContainerProps>(FlexContainer)`
  justify-content: center;
`;

const MemberRequestRedemptionModalComponent: React.SFC<Props> = (props) => {
  const {
    state,
    setState,
    dismiss,
    onTransactionsChanged,
    userNeoWallet,
    detailsForProfile: { user: { username } }
  } = props;

  const status = EnhancedUserRedemptionStatus.get(userNeoWallet.redemptionStatus);

  const hideModal = () => {
    setState(ss => ({ ...ss, isModalHidden: true }));
  };

  let title: React.ReactNode;
  let body: React.ReactNode;

  if (status.isWalletUnspecified()) {
    const nrveLink = <Link.About type="nrve"/>;
    const supportSiteLink = (
      <Link.Anchor href={externalUrls.narrativeSupportCommunity} target="_blank">
        {externalUrls.narrativeSupportCommunity}
      </Link.Anchor>
    );

    title = <FormattedMessage {...MemberRewardsMessages.HowToRedeemRewardPoints}/>;
    body = (
      <React.Fragment>
        <RedemptionParagraph>
          <FormattedMessage
            {...MemberRewardsMessages.PointRedemptionExplanationIntro}
            values={{nrveLink}}
          />
          <PointRedemptionExplanation />
        </RedemptionParagraph>
        <RedemptionParagraph style={{fontWeight: 'bold'}}>
          <FormattedMessage {...MemberRewardsMessages.ClickButtonToAddYourWallet}/>
        </RedemptionParagraph>
        <RedemptionParagraph>
          <FormattedMessage {...MemberRewardsMessages.IfYouHaveQuestions} values={{supportSiteLink}}/>
        </RedemptionParagraph>
        <RedemptionButton>
          <Button href={generatePath(WebRoute.MemberNeoWallet)} type="primary">
            <FormattedMessage {...MemberRewardsMessages.SetYourNEOWalletAddress}/>
          </Button>
        </RedemptionButton>
      </React.Fragment>
    );
  } else if (status.isWalletInWaitingPeriod()) {
    if (!userNeoWallet.waitingPeriodEndDatetime) {
      // todo:error-handling: We should always have a waitingPeriodEndDatetime with this status!
      return null;
    }

    const neoWalletLink = (
      <Link to={WebRoute.MemberNeoWallet}>
        <FormattedMessage {...MemberRewardsMessages.NeoWallet}/>
      </Link>
    );

    const availableDatetime = <LocalizedTime time={userNeoWallet.waitingPeriodEndDatetime}/>;

    body = (
      <FormattedMessage
        {...MemberRewardsMessages.YourNeoWalletIsInWaitingPeriod}
        values={{neoWalletLink, availableDatetime}}
      />
    );
  } else if (status.isHasPendingRedemption()) {
    const rewardTransactionsPath = generatePath(WebRoute.UserProfileRewardsTransactions, {username});

    let pendingTransactionText;
    if (props.history.location.pathname.startsWith(rewardTransactionsPath)) {
      pendingTransactionText = <FormattedMessage {...MemberRewardsMessages.PendingTransaction} />;

    } else {
      pendingTransactionText = (
        <Link to={rewardTransactionsPath}>
          <FormattedMessage {...MemberRewardsMessages.PendingTransaction}/>
        </Link>
      );
    }

    body = (
      <FormattedMessage
        {...MemberRewardsMessages.PendingTransactionWarning}
        values={{pendingTransactionText}}
      />
    );
  } else if (!status.isRedemptionAvailable()) {
    // todo:error-handling: We should report to the server that we came across an unrecognized redemption status.

    return null;
  } else {
    const onRedemptionRequested = () => {
      hideModal();
      onTransactionsChanged();

      const rewardTransactionsPath = generatePath(WebRoute.UserProfileRewardsTransactions, {username});

      // bl: if we are on the overview page, then redirect to transactions on complete.
      if (!props.history.location.pathname.startsWith(rewardTransactionsPath)) {
        props.history.push(rewardTransactionsPath);
      }
    };

    body = (
      <MemberRequestRedemptionForm
        onRedemptionRequested={onRedemptionRequested}
        userNeoWallet={userNeoWallet}
      />
    );
  }

  // bl: the modal needs to be destroyed in the parent every time it's closed, so we first hide the modal
  // and then let antd's afterClose handle cascading the event up to the parent once the modal has fully closed.
  return (
    <Modal
        visible={!state.isModalHidden}
        footer={null}
        onCancel={hideModal}
        afterClose={dismiss}
        destroyOnClose={true}
        title={title}
    >
      {body}
    </Modal>
  );
};

export const MemberRequestRedemptionModal = compose(
  withUserNeoWallet,
  branch((props: WithLoadingPlaceholderProps) => props.loading,
   renderComponent(() => null)
  ),
  MemberProfileConnect,
  withRouter,
  withState({})
)(MemberRequestRedemptionModalComponent) as React.ComponentClass<ParentProps>;
