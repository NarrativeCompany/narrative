import * as React from 'react';
import { compose } from 'recompose';
import {
  withUserNeoWallet,
  WithUserNeoWalletProps,
  withState,
  WithStateProps,
} from '@narrative/shared';
import { UpdateMemberNeoWalletModal } from './UpdateMemberNeoWalletModal';
import { withLoadingPlaceholder } from '../../../shared/utils/withLoadingPlaceholder';
import { WithCurrentUserProps, withExtractedCurrentUser } from '../../../shared/containers/withExtractedCurrentUser';
import { DeleteMemberNeoWalletModal } from './DeleteMemberNeoWalletModal';
import { EnhancedUserRedemptionStatus } from '../../../shared/enhancedEnums/userRedemptionStatus';
import { MemberNeoWalletDetails } from './MemberNeoWalletDetails';
import { Tooltip } from 'antd';
import { MemberNeoWalletMessages } from '../../../shared/i18n/MemberNeoWalletMessages';
import { FormattedMessage } from 'react-intl';
import { Link } from '../../../shared/components/Link';
import { generatePath } from 'react-router';
import { WebRoute } from '../../../shared/constants/routes';
import { CountDown } from '../../../shared/components/CountDown';
import { Icon } from '../../../shared/components/Icon';

interface State {
  updateWalletModalOpen?: boolean;
  deleteWalletModalOpen?: boolean;
}

type Props = WithUserNeoWalletProps &
  WithStateProps<State> &
  WithCurrentUserProps;

const MemberNeoWalletBodyComponent: React.SFC<Props> = (props) => {
  const {
    setState,
    userNeoWallet: { redemptionStatus, neoAddress, waitingPeriodEndDatetime },
    currentUser: { username },
    state: { updateWalletModalOpen, deleteWalletModalOpen }
  } = props;

  const status = EnhancedUserRedemptionStatus.get(redemptionStatus);

  // jw: if the wallet has a pending redemption we cannot allow them to change it, so let's output the simplest version
  if (status.isHasPendingRedemption()) {
    if (!neoAddress) {
      // todo:error-handling: should always have a neoAddress set if iot is pending a redemption
      return null;
    }

    return (
      <MemberNeoWalletDetails
        neoAddress={neoAddress}
        status={
          <span>
            <Link to={generatePath(WebRoute.UserProfileRewardsTransactions, {username})}>
              <FormattedMessage {...MemberNeoWalletMessages.PendingRedemption}/>
            </Link>
            <Tooltip title={<FormattedMessage {...MemberNeoWalletMessages.PendingRedemptionTooltip}/>}>
              <Icon type="info-circle" style={{marginLeft: 5}}/>
            </Tooltip>
          </span>
        }
      />
    );
  }

  // jw: we will need the update wallet modal for most other cases, so let's use that.
  const showUpdateWalletModal = () => {
    setState(ss => ({...ss, updateWalletModalOpen: true}));
  };
  const updateWalletModal = (
    <UpdateMemberNeoWalletModal
      visible={updateWalletModalOpen}
      dismiss={() => setState(ss => ({...ss, updateWalletModalOpen: undefined}))}
    />
  );

  // jw: if the address is unset then just render a sentence with a link to show the update modal.
  if (status.isWalletUnspecified()) {
    const addWalletLink = (
      <Link.Anchor onClick={showUpdateWalletModal}>
        <FormattedMessage {...MemberNeoWalletMessages.AddWallet}/>
      </Link.Anchor>
    );

    return (
      <React.Fragment>
        <FormattedMessage {...MemberNeoWalletMessages.YouDoNotHaveWalletSet} values={{addWalletLink}}/>
        {updateWalletModal}
      </React.Fragment>
    );
  }

  if (!neoAddress) {
    // todo:error-handling: We should always have a neoAddress at this point!
    return null;
  }

  // jw: from this point the user can update or delete their wallet, so things are a bit more streamlined now.
  let statusForDetails;
  if (status.isWalletInWaitingPeriod()) {
    if (!waitingPeriodEndDatetime) {
      // todo:error-handling: We should always have a waitingPeriodEndDatetime at this point!
      return null;
    }

    const countdown = <CountDown endTime={waitingPeriodEndDatetime} inline={true} />;

    statusForDetails = <FormattedMessage {...MemberNeoWalletMessages.PendingWaitingPeriod} values={{countdown}}/>;
  } else {
    statusForDetails = <FormattedMessage {...MemberNeoWalletMessages.WalletActive}/>;
  }

  return (
    <React.Fragment>
      <MemberNeoWalletDetails
        neoAddress={neoAddress}
        status={statusForDetails}
        updateHandler={showUpdateWalletModal}
        deleteHandler={() => setState(ss => ({...ss, deleteWalletModalOpen: true}))}
      />

      {updateWalletModal}

      <DeleteMemberNeoWalletModal
        visible={deleteWalletModalOpen}
        dismiss={() => setState(ss => ({...ss, deleteWalletModalOpen: undefined}))}
      />

    </React.Fragment>
  );
};

export const MemberNeoWalletBody = compose(
  withExtractedCurrentUser,
  withUserNeoWallet,
  withLoadingPlaceholder(),
  withState<State>({})
)(MemberNeoWalletBodyComponent) as React.ComponentClass<{}>;
