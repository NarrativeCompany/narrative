import { defineMessages } from 'react-intl';

export const AuctionDetailsMessages = defineMessages({
  NicheAuction: {
    id: 'auctionDetails.nicheAuction',
    defaultMessage: 'Niche Auction'
  },
  AllAuctions: {
    id: 'auctionDetails.allAuctions',
    defaultMessage: 'All Auctions'
  },
  BiddingHasEnded: {
    id: 'auctionDetails.biddingHasEnded',
    defaultMessage: 'Bidding has ended.'
  },
  Summary: {
    id: 'auctionDetails.summary',
    defaultMessage: 'Summary'
  },
  Status: {
    id: 'auctionSummarySection.status',
    defaultMessage: 'Status'
  },
  SaleOngoing: {
    id: 'auctionSummarySection.saleOngoing',
    defaultMessage: 'Sale Ongoing'
  },
  SaleEnded: {
    id: 'auctionSummarySection.saleEnded',
    defaultMessage: 'Sale Ended'
  },
  BidPeriodStart: {
    id: 'auctionSummarySection.bidPeriodStart',
    defaultMessage: 'Bid Period Start'
  },
  BidPeriodEnd: {
    id: 'auctionSummarySection.bidPeriodEnd',
    defaultMessage: 'Bid Period End'
  },
  CurrentLeadingBid: {
    id: 'auctionSummarySection.currentLeadingBid',
    defaultMessage: 'Current Leading Bid'
  },
  WinningBid: {
    id: 'auctionSummarySection.winningBid',
    defaultMessage: 'Winning Bid'
  },
  YourMaxBid: {
    id: 'auctionSummarySection.yourMaxBid',
    defaultMessage: 'Your Max Bid'
  },
  YourHighestBid: {
    id: 'auctionSummarySection.yourHighestBid',
    defaultMessage: 'Your Highest Bid'
  },
  BidHistory: {
    id: 'auctionBidsSection.bidHistory',
    defaultMessage: 'Bid History'
  },
  NoBidsYet: {
    id: 'auctionBidsSection.noBidsYet',
    defaultMessage: 'No Bids Yet'
  },
  FailedToPayBidStatus: {
    id: 'auctionBidStatusTag.failedToPayBidStatus',
    defaultMessage: 'Failed to Pay'
  },
  LeadingBidStatus: {
    id: 'auctionBidStatusTag.leadingBidStatus',
    defaultMessage: 'Leading'
  },
  WonBidStatus: {
    id: 'auctionBidStatusTag.wonBidStatus',
    defaultMessage: 'Won'
  },
  Bid: {
    id: 'auctionActionCard.bid',
    defaultMessage: 'Bid'
  },
  CurrentBid: {
    id: 'auctionBiddingForm.currentBid',
    defaultMessage: 'Current Bid'
  },
  MinimumBid: {
    id: 'auctionBiddingForm.minimumBid',
    defaultMessage: 'Minimum Bid'
  },
  MakeBid: {
    id: 'auctionBiddingForm.makeBid',
    defaultMessage: 'Make Bid'
  },
  MaxNrveBidPlaceholder: {
    id: 'auctionBiddingForm.maxNrveBidPlaceholder',
    defaultMessage: 'Your max bid'
  },
  MaxNrveBidSuffix: {
    id: 'auctionBiddingForm.maxNrveBidSuffix',
    defaultMessage: 'NRVE'
  },
  PlaceBid: {
    id: 'auctionBiddingForm.placeBid',
    defaultMessage: 'Place Bid'
  },
  HighestBidder: {
    id: 'auctionCurrentUserStatusCard.highestBidder',
    defaultMessage: 'Highest Bidder'
  },
  CongratsOnBeingHighestBidder: {
    id: 'auctionCurrentUserStatusCard.congratsOnBeingHighestBidder',
    defaultMessage: 'Congrats! You are the highest bidder at {nrveValue}!'
  },
  Outbid: {
    id: 'auctionCurrentUserStatusCard.outbid',
    defaultMessage: 'Outbid'
  },
  YouveBeenOutbid: {
    id: 'auctionCurrentUserStatusCard.youveBeenOutbid',
    defaultMessage: 'You’ve been outbid.'
  },
  NotSignedIn: {
    id: 'auctionCurrentUserStatusCard.notSignedIn',
    defaultMessage: 'Not Signed In'
  },
  MustSignInMessage: {
    id: 'auctionCurrentUserStatusCard.mustSignInMessage',
    defaultMessage: 'You must {signInLink} to bid.'
  },
  NicheSlotsFull: {
    id: 'auctionCurrentUserStatusCard.nicheSlotsFull',
    defaultMessage: 'Niche Slots Full'
  },
  NicheSlotsFullMessage: {
    id: 'auctionCurrentUserStatusCard.nicheSlotsFullMessage',
    defaultMessage: 'You have no available Niche slots, so you cannot participate in this auction.'
  },
  LowReputationMessage: {
    id: 'auctionCurrentUserStatusCard.lowReputationMessage',
    defaultMessage: 'You are Low Reputation, so you cannot participate in this auction.'
  },
  LowReputationCertMessage: {
    id: 'activeInvoiceStatusCard.lowReputationCertMessage',
    defaultMessage: 'Get {certLink} to boost your reputation immediately!'
  },
  ConductNegativeMessage: {
    id: 'auctionCurrentUserStatusCard.conductNegativeMessage',
    defaultMessage: 'You are Conduct Negative, so you cannot participate in this auction.'
  },
  ConductNegativeCertMessage: {
    id: 'auctionCurrentUserStatusCard.lconductNegativeCertMessage',
    defaultMessage: 'Get {certLink} to end your Conduct Negative status immediately!'
  },
  InvoiceDue: {
    id: 'activeInvoiceStatusCard.invoiceDue',
    defaultMessage: 'Invoice Due'
  },
  Invoice: {
    id: 'activeInvoiceStatusCard.invoice',
    defaultMessage: 'invoice'
  },
  YouHaveAnExistingInvoice: {
    id: 'activeInvoiceStatusCard.youHaveAnExistingInvoice',
    defaultMessage: 'You have an {invoiceLink} pending payment.'
  },
  ExistingInvoiceInfo: {
    id: 'activeInvoiceStatusCard.existingInvoiceInfo',
    defaultMessage: 'You have until {expirationDatetime} to pay this invoice.'
  },
  SecurityDepositForNicheAuction: {
    id: 'auctionPlaceSecurityDepositStatusCard.securityDepositForNicheAuction',
    defaultMessage: 'Security deposit for {nicheName} auction.'
  },
  BidSecurityDepositRequired: {
    id: 'auctionPLaceSecurityDepositStatusCard.bidSecurityDepositRequired',
    defaultMessage: 'Bid Security Deposit Required'
  },
  BidSecurityDepositRequiredMessage: {
    id: 'auctionPlaceSecurityDepositStatusCard.bidSecurityDepositRequiredMessage',
    defaultMessage: 'Because your Reputation Score is less than 50, you are required to pay a security deposit of' +
      ' {usdAmount} before you can place a bid.'
  },
  YourCurrentRepIs: {
    id: 'auctionPLaceSecurityDepositStatusCard.yourCurrentRepIs',
    defaultMessage: 'Your current Reputation Score is {totalScore}.'
  },
  ConsiderCertificationToBoostScore: {
    id: 'auctionPlaceSecurityDepositStatusCard.considerCertificationToBoostScore',
    defaultMessage: 'Consider {certLink} to boost your score by 30 points.'
  },
  PaySecurityDepositToEnableBidding: {
    id: 'auctionPlaceSecurityDepositStatusCard.paySecurityDepositNow',
    defaultMessage: 'To pay the security deposit and enable bidding on this Niche, click on the PayPal button below.'
  },
});
