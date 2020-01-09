import * as React from 'react';
import { compose, withHandlers } from 'recompose';
import {
  BidOnNichesPermission,
  FiatPaymentProcessorType,
  NicheAuction,
  PayPalCheckoutDetails, User,
  withPlaceSecurityDepositOnNicheAuction,
  WithPlaceSecurityDepositOnNicheAuctionProps
} from '@narrative/shared';
import { withFiatPaymentButton, WithFiatPaymentButtonProps } from '../../../../shared/containers/withFiatPaymentButton';
import { AuctionStatusCard } from '../../../../shared/components/auction/AuctionStatusCard';
import { FormattedMessage, injectIntl, InjectedIntlProps } from 'react-intl';
import { AuctionDetailsMessages } from '../../../../shared/i18n/AuctionDetailsMessages';
import { PayWithPayPalButton } from '../../../../shared/components/invoice/PayWithPayPalButton';
import { getCertificationMessage } from '../../../../shared/utils/revokeReasonMessagesUtil';
import { Paragraph } from '../../../../shared/components/Paragraph';
import { Block } from '../../../../shared/components/Block';
import { WithUpdateAuctionDetailHandler } from '../../../../shared/containers/withUpdateableAuctionDetail';

interface ParentProps extends WithUpdateAuctionDetailHandler {
  bidOnNiches: BidOnNichesPermission;
  currentUser: User;
  auction: NicheAuction;
  payPalCheckoutDetails: PayPalCheckoutDetails;
}

type Props = ParentProps &
  WithFiatPaymentButtonProps &
  InjectedIntlProps;

const AuctionPlaceSecurityDepositStatusCardComponent: React.SFC<Props> = (props) => {
  const {
    payPalCheckoutDetails,
    processingPayment,
    onFiatPaymentToken,
    currentUser,
    intl: { formatMessage },
    auction: { niche }
  } = props;

  const { usdAmount } = payPalCheckoutDetails;

  return (
    <AuctionStatusCard color="primaryOrange" title={AuctionDetailsMessages.BidSecurityDepositRequired}>
      <Paragraph marginBottom="large" textAlign="center">
        <FormattedMessage {...AuctionDetailsMessages.BidSecurityDepositRequiredMessage} values={{usdAmount}} />
      </Paragraph>
      <Paragraph marginBottom="large" textAlign="center">
        {getCertificationMessage(
          AuctionDetailsMessages.YourCurrentRepIs,
          AuctionDetailsMessages.ConsiderCertificationToBoostScore,
          'certification',
          currentUser
        )}
      </Paragraph>
      <Paragraph marginBottom="large" textAlign="center">
        <FormattedMessage {...AuctionDetailsMessages.PaySecurityDepositToEnableBidding} />
      </Paragraph>
      <Block style={{textAlign: 'center'}}>
        <PayWithPayPalButton
          checkoutDetails={payPalCheckoutDetails}
          paymentDescription={formatMessage(
            AuctionDetailsMessages.SecurityDepositForNicheAuction,
            {nicheName: niche.name}
          )}
          handlePaymentId={
            (paymentToken: string) => onFiatPaymentToken(paymentToken, FiatPaymentProcessorType.PAYPAL)
          }
          processingPayment={processingPayment}
        />
      </Block>
    </AuctionStatusCard>
  );
};

export const AuctionPlaceSecurityDepositStatusCard = compose(
  withPlaceSecurityDepositOnNicheAuction,
  withHandlers({
    processFiatPaymentToken: (props: WithPlaceSecurityDepositOnNicheAuctionProps & ParentProps) =>
      async (paymentToken: string, processorType: FiatPaymentProcessorType) =>
    {
      const { placeSecurityDepositOnNicheAuction, auction, handleNewAuctionDetail } = props;

      // jw: it is vital that we await the response from this query.
      const newAuction = await placeSecurityDepositOnNicheAuction({ processorType, paymentToken }, auction.oid);

      // jw: we were successful if we got a invoice.
      const success = newAuction !== undefined && newAuction !== null;

      if (success && handleNewAuctionDetail) {
        handleNewAuctionDetail(newAuction);
      }

      return success;
    }
  }),
  withFiatPaymentButton,
  injectIntl
)(AuctionPlaceSecurityDepositStatusCardComponent) as React.ComponentClass<ParentProps>;
