import * as React from 'react';
import { compose } from 'recompose';
import { withState, WithStateProps } from '@narrative/shared';
import { FormattedMessage } from 'react-intl';
import { RewardsHeaderProps } from '../../../../shared/components/rewards/RewardsHeader';
import { MemberRewardsMessages } from '../../../../shared/i18n/MemberRewardsMessages';
import styled from '../../../../shared/styled';
import { Button, ButtonProps } from '../../../../shared/components/Button';
import { MemberRequestRedemptionModal } from './MemberRequestRedemptionModal';
import { MemberRewardsTransactionsChangedHandler } from '../MemberRewards';
import { MemberProfileCurrentUserRewardsHeaderTitle } from './MemberProfileCurrentUserRewardsHeaderTitle';

type ParentProps = MemberRewardsTransactionsChangedHandler & {
  renderCycle: number;
};

interface State {
  isRedeemModalVisible?: boolean;
}

type CurrentUserProps = ParentProps & RewardsHeaderProps & WithStateProps<State>;

const StyledButton = styled<ButtonProps>(Button)`
  &.ant-btn {
    display: block;
  }
`;

const MemberProfileCurrentUserRewardsHeaderComponent: React.SFC<CurrentUserProps> = (props) => {
  const { state, setState, onTransactionsChanged, renderCycle, ...ownProps } = props;

  const dismissModal = () => {
    setState(ss => ({ ...ss, isRedeemModalVisible: undefined }));
  };

  return (
    <React.Fragment>
      <MemberProfileCurrentUserRewardsHeaderTitle key={renderCycle} {...ownProps} rightColumn={
        <StyledButton type="green" onClick={() => setState(ss => ({ ...ss, isRedeemModalVisible: true }))}>
          <FormattedMessage {...MemberRewardsMessages.Redeem}/>
        </StyledButton>
      }/>
      {/* bl: remove the modal component entirely when it's dismissed so that it always gets reloaded. */}
      {state.isRedeemModalVisible &&
        <MemberRequestRedemptionModal
          dismiss={dismissModal}
          onTransactionsChanged={onTransactionsChanged}
        />
      }
    </React.Fragment>
  );
};

export const MemberProfileCurrentUserRewardsHeader = compose(
  withState({})
)(MemberProfileCurrentUserRewardsHeaderComponent) as React.ComponentClass<ParentProps>;
