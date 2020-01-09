import { defineMessages } from 'react-intl';

export const BidCardProgressBarMessages = defineMessages({
  WinningBidMessage: {
    id: 'bidCardProgressBar.winningBidMessage',
    defaultMessage: 'You are the highest bidder!'
  },
  OutBidMessage: {
    id: 'bidCardProgressBar.outBidMessage',
    defaultMessage: 'You have been outbid!'
  },
  NoBidsMessage: {
    id: 'bidCardProgressBar.noBidsMessage',
    defaultMessage: 'No Bids'
  },
  TotalBidsMessage: {
    id: 'bidCardProgressBar.totalBidsMessage',
    defaultMessage: '{totalBids, number} {totalBids, plural, one {Bid} other {Bids}}'
  }
});
