import * as React from 'react';
import { NRVE } from '../../../../shared/components/NRVE';
import { LocalizedNumber } from '../../../../shared/components/LocalizedNumber';
import { MemberLink } from '../../../../shared/components/user/MemberLink';
import { LocalizedTime } from '../../../../shared/components/LocalizedTime';
import { AuctionBidStatusTag, AuctionBidTagProps } from './AuctionBidStatusTag';
import { FlexContainer } from '../../../../shared/styled/shared/containers';
import styled from '../../../../shared/styled';
import { Paragraph } from '../../../../shared/components/Paragraph';

const BidContainer = styled(FlexContainer)`
  padding: 10px;
`;

const BidNumberContainer = styled(Paragraph)`
  width: 50px;
`;

const BidDetailsContainer = styled(FlexContainer)`
  width: 100%;
`;

const BidAmountContainer = styled(Paragraph)`
  width: 150px;
`;

interface Props extends AuctionBidTagProps {
  bidNumber: number;
}

export const AuctionBid: React.SFC<Props> = (props) => {
  const { bidNumber, bid, ...tagProps } = props;

  return (
    <BidContainer centerAll={true} className="auction-bid-container">
      <BidNumberContainer size="large" color="dark">
        <LocalizedNumber value={bidNumber} />
      </BidNumberContainer>

      <BidDetailsContainer column={true}>
        <FlexContainer>
          <div>
            <MemberLink user={bid.bidder}/>
          </div>
          <AuctionBidStatusTag bid={bid} {...tagProps} />
        </FlexContainer>
        <Paragraph size="small" color="light">
          <LocalizedTime time={bid.bidDatetime} />
        </Paragraph>
      </BidDetailsContainer>

      <BidAmountContainer size="large">
        <NRVE amount={bid.bidAmount.nrve} />
      </BidAmountContainer>
    </BidContainer>
  );
};
