import * as React from 'react';
import { AuctionDetailProps } from './AuctionDetails';
import {
  BidOnNichesRevokeReason,
  BidStatus
} from '@narrative/shared';
import { NRVE } from '../../../../shared/components/NRVE';
import { AuctionDetailsMessages } from '../../../../shared/i18n/AuctionDetailsMessages';
import { FormattedMessage } from 'react-intl';
import { compose } from 'recompose';
import {
  withExtractedCurrentUser,
  WithExtractedCurrentUserProps
} from '../../../../shared/containers/withExtractedCurrentUser';
import { ModalConnect, ModalName, ModalStoreProps } from '../../../../shared/stores/ModalStore';
import { Link } from '../../../../shared/components/Link';
import { AuctionStatusCard } from '../../../../shared/components/auction/AuctionStatusCard';
import {
  WithUpdateAuctionDetailHandler,
  WithUpdateFromBidProps
} from '../../../../shared/containers/withUpdateableAuctionDetail';
import { RevokeReasonMessages } from '../../../../shared/i18n/RevokeReasonMessages';
import { AuctionPlaceSecurityDepositStatusCard } from './AuctionPlaceSecurityDepositStatusCard';
import { getCertificationMessage } from '../../../../shared/utils/revokeReasonMessagesUtil';
import { MemberReputationMessages } from '../../../../shared/i18n/MemberReputationMessages';

type ParentProps = AuctionDetailProps &
  WithUpdateAuctionDetailHandler &
  WithUpdateFromBidProps;

type Props = ParentProps &
  WithExtractedCurrentUserProps &
  ModalStoreProps;

const AuctionCurrentUserStatusCardComponent: React.SFC<Props> = (props) => {
  const {
    auction,
    currentUser,
    currentUserLatestBidStatus,
    currentUserLatestMaxNrveBid,
    currentUserGlobalPermissions
  } = props;

  const bidOnNiches =
    currentUserGlobalPermissions &&
    currentUserGlobalPermissions.bidOnNiches;

  // jw: if the niche is not open for bidding, let's go ahead and short out
  if (!auction.openForBidding) {
    return null;
  }

  // jw: if they are not logged in, let's give them a prompt to change that.
  if (!currentUser) {
    const { modalStoreActions } = props;

    const signInLink = (
      <Link.Anchor onClick={() => modalStoreActions.updateModalVisibility(ModalName.login)}>
        <FormattedMessage {...RevokeReasonMessages.SignIn} />
      </Link.Anchor>
    );

    return (
      <AuctionStatusCard
        color="primaryOrange"
        title={AuctionDetailsMessages.NotSignedIn}
        message={<FormattedMessage {...AuctionDetailsMessages.MustSignInMessage} values={{signInLink}} />}
      />
    );
  }

  // jw: since we know we are dealing with a logged in user, let's make sure they have the right to bid
  if (bidOnNiches && !bidOnNiches.granted) {
    switch (bidOnNiches.revokeReason) {
      case (BidOnNichesRevokeReason.NICHE_SLOTS_FULL):
        const isLeadingBidder =
          currentUserLatestBidStatus &&
          currentUserLatestBidStatus === BidStatus.LEADING;

        // jw: if this is the leading bidder then they can stll bid on the auction.
        if (isLeadingBidder) {
          break;
        }

        return (
          <AuctionStatusCard
            color="primaryRed"
            title={AuctionDetailsMessages.NicheSlotsFull}
            message={<FormattedMessage {...AuctionDetailsMessages.NicheSlotsFullMessage} />}
          />
        );
      case (BidOnNichesRevokeReason.SECURITY_DEPOSIT_REQUIRED):
        const { currentUserBypassesSecurityDepositRequirement, securityDepositPayPalCheckoutDetails } = props;
        // jw: first, if the user bypasses the security deposit requirement then we are free to drop down
        if (currentUserBypassesSecurityDepositRequirement) {
          break;
        }

        // jw: next, if we have payment details, then we need to give them the chance to pay.
        if (securityDepositPayPalCheckoutDetails) {

          const { handleNewAuctionDetail } = props;

          return (
            <AuctionPlaceSecurityDepositStatusCard
              auction={auction}
              payPalCheckoutDetails={securityDepositPayPalCheckoutDetails}
              bidOnNiches={bidOnNiches}
              currentUser={currentUser}
              handleNewAuctionDetail={handleNewAuctionDetail}
            />
          );
        }

        // jw:todo:error-handling: If we got a SECURITY_DEPOSIT_REQUIRED revoke reason that means that either the user
        //         should be able to make a fiat payment, or they bypass that requirement... We should never get here.
        return null;

      case (BidOnNichesRevokeReason.LOW_REPUTATION):
        return (
          <AuctionStatusCard
            color="primaryRed"
            title={MemberReputationMessages.LowReputationTitle}
            message={getCertificationMessage(
              AuctionDetailsMessages.LowReputationMessage,
              AuctionDetailsMessages.LowReputationCertMessage,
              'certified',
              currentUser
            )}
          />
        );
      case (BidOnNichesRevokeReason.CONDUCT_NEGATIVE):
        return (
          <AuctionStatusCard
            color="primaryRed"
            title={MemberReputationMessages.ConductNegativeReputationTitle}
            message={getCertificationMessage(
              AuctionDetailsMessages.ConductNegativeMessage,
              AuctionDetailsMessages.ConductNegativeCertMessage,
              'certified',
              currentUser
            )}
          />
        );
      default:
        // todo:error-handling: we should report this unexpected case!
        return null;
    }
  }

  if (!currentUserLatestMaxNrveBid) {
    // todo:error-handling: we should report this unexpected case!
    return null;
  }

  switch (currentUserLatestBidStatus) {
    case (BidStatus.LEADING):
      const nrveValue = <strong><NRVE amount={currentUserLatestMaxNrveBid.nrve} /></strong>;

      return (
        <AuctionStatusCard
          color="primaryGreen"
          title={AuctionDetailsMessages.HighestBidder}
          message={<FormattedMessage {...AuctionDetailsMessages.CongratsOnBeingHighestBidder} values={{nrveValue}} />}
        />
      );

    case (BidStatus.OUTBID):
      const { updateFromBid } = props;

      // jw: note: We need the key on this so that the component will re-render and generate a new state.
      return (
        <AuctionStatusCard
          key={currentUserLatestMaxNrveBid.nrve}
          title={AuctionDetailsMessages.Outbid}
          color={updateFromBid ? 'secondaryOrange' : 'primaryOrange'}
          transitionToColor={updateFromBid ? 'primaryOrange' : undefined}
          message={<FormattedMessage {...AuctionDetailsMessages.YouveBeenOutbid} />}
        />
      );

    default:
      return null;
  }
};

export const AuctionCurrentUserStatusCard = compose(
  withExtractedCurrentUser,
  ModalConnect(ModalName.login)
)(AuctionCurrentUserStatusCardComponent) as React.ComponentClass<ParentProps>;
