import * as React from 'react';
import { compose, withProps } from 'recompose';
import { NicheCardProgressBar } from '../components/NicheCardProgressBar';
import { themeColors } from '../../../shared/styled/theme';
import { NicheAuction } from '@narrative/shared';
import { FlexContainer, FlexContainerProps } from '../../../shared/styled/shared/containers';
import { injectIntl, InjectedIntlProps, FormattedMessage } from 'react-intl';
import { BidCardProgressBarMessages } from '../../../shared/i18n/BidCardProgressBarMessages';
import { Paragraph, ParagraphProps } from '../../../shared/components/Paragraph';
import styled from '../../../shared/styled';

const ProgressBarWrapper = styled<FlexContainerProps>(FlexContainer)`
  position: relative;
  display: flex !important;
`;

const ProgressBarMessage =
  styled<{totalBidCount: number} & ParagraphProps>(({totalBidCount, ...rest}) =>
    <Paragraph {...rest}>{rest.children}</Paragraph>
  )`
    position: absolute;
    top: 0px;
    color: ${props => props.totalBidCount > 0 && '#fff'};
  `;

interface WithProps {
  progressBarData: ProgressBarData;
}

interface ParentProps {
  auction: NicheAuction;
  pendingPayment?: boolean;
  currentUserOid?: string;
}

type Props =
  ParentProps &
  WithProps &
  InjectedIntlProps;

const BidCardProgressBarComponent: React.SFC<Props> = (props) => {
  const { pendingPayment, progressBarData, auction } = props;

  return (
    <ProgressBarWrapper centerAll={true}>
      <NicheCardProgressBar
        percent={progressBarData.percent}
        strokeColor={progressBarData.color}
      />

      {!pendingPayment &&
      <ProgressBarMessage totalBidCount={auction.totalBidCount}>
        {progressBarData.message}
      </ProgressBarMessage>}
    </ProgressBarWrapper>
  );
};

interface ProgressBarData {
  message?: React.ReactNode;
  color?: string;
  percent: number;
}

const getProgressBarData = (props: Props): ProgressBarData => {
  const { currentUserOid, auction, pendingPayment } = props;
  const auctionHasBids = auction.totalBidCount > 0;

  const percent = auctionHasBids || pendingPayment ? 100 : 0;
  let color;
  let message;

  const leadingBid =
    auction &&
    auction.leadingBid;

  if (pendingPayment) {
    color = themeColors.primaryGreen;
  } else if (!!(auction.currentRoleOutbid)) {
    color = themeColors.primaryRed;
    message = <FormattedMessage {...BidCardProgressBarMessages.OutBidMessage}/>;
  } else if (currentUserOid && leadingBid && (currentUserOid === leadingBid.bidder.oid)) {
    color = themeColors.primaryGreen;
    message = <FormattedMessage {...BidCardProgressBarMessages.WinningBidMessage}/>;
  } else {
     if (auctionHasBids) {
       color = themeColors.secondaryBlue;
       message = (
         <FormattedMessage
           {...BidCardProgressBarMessages.TotalBidsMessage}
           values={{totalBids: auction.totalBidCount}}
         />
       );
     } else {
       color = undefined;
       message = <FormattedMessage {...BidCardProgressBarMessages.NoBidsMessage}/>;
     }
  }

  return { percent, color, message };
};

export const AuctionCardProgressBar = compose(
  injectIntl,
  withProps((props: Props) => {
    const progressBarData = getProgressBarData(props);

    return { progressBarData };
  })
)(BidCardProgressBarComponent) as React.ComponentClass<ParentProps>;
