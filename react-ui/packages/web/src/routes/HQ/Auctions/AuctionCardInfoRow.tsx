import * as React from 'react';
import { Col, Icon, Row } from 'antd';
import { Paragraph } from '../../../shared/components/Paragraph';
import { CountDown } from '../../../shared/components/CountDown';
import { FlexContainer, FlexContainerProps } from '../../../shared/styled/shared/containers';
import { FormattedMessage } from 'react-intl';
import { BidCardInfoRowMessages } from '../../../shared/i18n/BidCardInfoRowMessages';
import styled from '../../../shared/styled/index';
import { NRVE } from '../../../shared/components/NRVE';

const InfoRowWrapper = styled<FlexContainerProps>(FlexContainer)`
  margin-top: auto;
  margin-bottom: 25px;
  width: 100%;
  
  .ant-row {
    width: 100%;
  }
`;

interface ParentProps {
  pendingPayment?: boolean;
  endTime: string | null;
  currentBid: string | null;
  totalBidCount: number;

}

export const AuctionCardInfoRow: React.SFC<ParentProps> = (props) => {
  const { totalBidCount, endTime, currentBid, pendingPayment } = props;
  let InfoRowBottom;

  if (pendingPayment) {
    InfoRowBottom = (
      <Paragraph color="success">
        <FormattedMessage {...BidCardInfoRowMessages.PaymentPending}/>
      </Paragraph>
    );
  } else if (endTime) {
    InfoRowBottom = (
      <React.Fragment>
        <Icon type="hourglass" style={{ marginRight: 5 }}/>
        <Paragraph color="error">
          <CountDown endTime={endTime}/>
        </Paragraph>
      </React.Fragment>
    );
  } else {
    InfoRowBottom = null;
  }

  return (
    <InfoRowWrapper>
      <Row justify="center" type="flex" style={{ width: '100%' }}>
        <Col span={24} style={{ marginBottom: 5 }}>
          <FlexContainer justifyContent="center">
            <Paragraph color="light">
              {pendingPayment &&
              <FormattedMessage {...BidCardInfoRowMessages.WinningBid}/>
              }
              {!pendingPayment && totalBidCount > 0 &&
              <FormattedMessage {...BidCardInfoRowMessages.CurrentBid}/>
              }
              {!pendingPayment && totalBidCount === 0 &&
              <FormattedMessage {...BidCardInfoRowMessages.MinimumBid}/>
              }
              :&nbsp;&nbsp;
            </Paragraph>

            <Paragraph color="dark">
              <NRVE amount={currentBid}/>
            </Paragraph>
          </FlexContainer>
        </Col>

        {InfoRowBottom &&
        <Col span={24}>
          <FlexContainer centerAll={true}>
            {InfoRowBottom}
          </FlexContainer>
        </Col>}
      </Row>
    </InfoRowWrapper>
  );
};
