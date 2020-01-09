import * as React from 'react';
import {
  NicheAuction,
  BidStatus,
  AuctionOidProps,
  PayPalCheckoutDetails,
  NrveUsdValue
} from '@narrative/shared';
import { compose, withProps } from 'recompose';
import { RouteComponentProps } from 'react-router';
import { NotFound } from '../../../../shared/components/NotFound';
import { SecondaryDetailsViewWrapper } from '../../../../shared/components/SecondaryDetailsViewWrapper';
import { FormattedMessage, InjectedIntlProps, injectIntl } from 'react-intl';
import { Link } from '../../../../shared/components/Link';
import { WebRoute } from '../../../../shared/constants/routes';
import { SimilarNichesSidebarCard } from '../../../../shared/components/sidebar/SimilarNichesSidebarCard';
import { AuctionDetailsMessages } from '../../../../shared/i18n/AuctionDetailsMessages';
import { NicheSlotCountSidebarCard } from '../../../../shared/components/sidebar/NicheSlotCountSidebarCard';
import { AuctionSummarySection } from './AuctionSummarySection';
import { AuctionBidsSection } from './AuctionBidsSection';
import { AuctionActionCard } from '../../../../shared/components/auction/AuctionActionCard';
import {
  withUpdateableAuctionDetail,
  WithUpdateableAuctionDetailProps
} from '../../../../shared/containers/withUpdateableAuctionDetail';
import { SEO } from '../../../../shared/components/SEO';
import { SEOMessages } from '../../../../shared/i18n/SEOMessages';
import { ActiveInvoiceStatusCard } from '../../../../shared/components/auction/ActiveInvoiceStatusCard';
import { DetailsGradientBox } from '../../../../shared/components/DetailsGradientBox';
import { LoadingViewWrapper } from '../../../../shared/components/LoadingViewWrapper';

export interface AuctionProps {
  auction: NicheAuction;
}

export interface AuctionDetailProps extends AuctionProps {
  currentUserLatestBidStatus?: BidStatus;
  currentUserLatestMaxNrveBid?: NrveUsdValue;
  currentUserActiveInvoiceOid?: string;
  currentUserBypassesSecurityDepositRequirement?: boolean;
  securityDepositPayPalCheckoutDetails?: PayPalCheckoutDetails;
}

type Props = WithUpdateableAuctionDetailProps &
  InjectedIntlProps;

const AuctionDetailsComponent: React.SFC<Props> = (props) => {
  const { intl, auction, currentUserActiveInvoiceOid } = props;

  if (!auction) {
    return <NotFound/>;
  }

  return (
    <SecondaryDetailsViewWrapper
      channel={auction.niche}
      iconType="bid"
      title={<FormattedMessage {...AuctionDetailsMessages.NicheAuction} />}
      listLink={<Link to={WebRoute.Auctions}><FormattedMessage {...AuctionDetailsMessages.AllAuctions} /></Link>}
      status={auction.openForBidding ? undefined : AuctionDetailsMessages.BiddingHasEnded}
      sidebarItems={
        <React.Fragment>
          <NicheSlotCountSidebarCard />
          <SimilarNichesSidebarCard niche={auction.niche} />
        </React.Fragment>
      }
    >
      <SEO
        title={intl.formatMessage(SEOMessages.AuctionDetailsTitle) + ' - ' + auction.niche.name}
        description={auction.niche.description}
      />

      {/* jw: If the user has a outstanding invoice, let's include the status for that first  */}
      {currentUserActiveInvoiceOid && <ActiveInvoiceStatusCard invoiceOid={currentUserActiveInvoiceOid} />}

      {/* jw: let's put the action card near the top  */}
      <AuctionActionCard {...props} />

      {/* jw: let's include the summary section next. */}
      <AuctionSummarySection {...props} />

      {/* jw: let's include the bids section last. */}
      <AuctionBidsSection auction={auction} />

    </SecondaryDetailsViewWrapper>
  );
};

export default compose(
  injectIntl,
  withProps((props: RouteComponentProps<AuctionOidProps>) => {
    return { auctionOid: props.match.params.auctionOid };
  }),
  withUpdateableAuctionDetail(() => <LoadingViewWrapper gradientBox={<DetailsGradientBox color="blue"/>}/>)
)(AuctionDetailsComponent) as React.ComponentClass<{}>;
