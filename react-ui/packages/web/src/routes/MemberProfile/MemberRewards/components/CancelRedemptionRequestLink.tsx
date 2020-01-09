import * as React from 'react';
import { compose, withHandlers } from 'recompose';
import {
  withCancelRedemptionRequest, WithCancelRedemptionRequestProps, UserRewardTransaction, handleFormlessServerOperation
} from '@narrative/shared';
import { FormattedMessage, injectIntl, InjectedIntlProps } from 'react-intl';
import { Popconfirm } from 'antd';
import { Link } from '../../../../shared/components/Link';
import { MemberRewardsMessages } from '../../../../shared/i18n/MemberRewardsMessages';

// jw: Note: This component assumes that the caller has done the security check for the currentUsers ability to edit.

interface WithHandlers {
  handleCancelRedemptionRequest: () => void;
}

interface ParentProps {
  transaction: UserRewardTransaction;
  onCanceledRedemptionRequest: () => void;
}

const CancelRedemptionRequestLinkComponent: React.SFC<WithHandlers> = (props) => {
  const { handleCancelRedemptionRequest } = props;

  return (
    <Popconfirm
      title={<FormattedMessage {...MemberRewardsMessages.CancelRedemptionRequestConfirmation} />}
      icon={null}
      okText={<FormattedMessage {...MemberRewardsMessages.CancelRedemptionRequestYesText} />}
      cancelText={<FormattedMessage {...MemberRewardsMessages.CancelRedemptionRequestNoText} />}
      onConfirm={handleCancelRedemptionRequest}
      placement="bottomRight"
    >
      <Link.Anchor size="small">
        <FormattedMessage {...MemberRewardsMessages.CancelLinkText} />
      </Link.Anchor>
    </Popconfirm>
  );
};

type HandleCancelRedemptionRequestProps =
  ParentProps &
  WithCancelRedemptionRequestProps &
  InjectedIntlProps;

export const CancelRedemptionRequestLink = compose(
  withCancelRedemptionRequest,
  injectIntl,
  withHandlers({
    handleCancelRedemptionRequest: (props: HandleCancelRedemptionRequestProps) => async () => {
      const { transaction, onCanceledRedemptionRequest, cancelRedemptionRequest } = props;

      await handleFormlessServerOperation(() => cancelRedemptionRequest({redemptionOid: transaction.oid}));

      onCanceledRedemptionRequest();
    }
  })
)(CancelRedemptionRequestLinkComponent) as React.ComponentClass<ParentProps>;
