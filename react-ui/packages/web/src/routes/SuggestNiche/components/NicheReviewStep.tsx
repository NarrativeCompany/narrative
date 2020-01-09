import * as React from 'react';
import { FormattedMessage } from 'react-intl';
import { Col, Row } from 'antd';
import { Link } from '../../../shared/components/Link';
import { NicheCard } from '../../HQ/components/NicheCard';
import { NicheCardProgressBar } from '../../HQ/components/NicheCardProgressBar';
import { NicheCardUser } from '../../HQ/components/NicheCardUser';
import { ChannelCardTitleAndDesc } from '../../HQ/components/ChannelCardTitleAndDesc';
import { StepWrapper } from './FormStepWrapper';
import { Heading } from '../../../shared/components/Heading';
import { Paragraph } from '../../../shared/components/Paragraph';
import { NicheReviewStepMessages } from '../../../shared/i18n/NicheReviewStepMessages';
import { WebRoute } from '../../../shared/constants/routes';
import { Button } from '../../../shared/components/Button';
import { themeColors, themeTypography } from '../../../shared/styled/theme';
import {
  User,
  Referendum,
  Niche
} from '@narrative/shared';
import styled from '../../../shared/styled';
import { generatePath } from 'react-router';

const ParagraphWrapper = styled.div`
  margin-bottom: 30px;
`;

interface ParentProps {
  currentUser?: User;
  referendum?: Referendum;
}

export const NicheReviewStep: React.SFC<ParentProps> = (props) => {
  const { currentUser, referendum } = props;

  // jw: if we do not have a referendum, then a niche was not created!
  if (!referendum) {
    return null;
  }

  if (!currentUser) {
    // jw:todo:error-handling: Need to report to the server that a guest made it this far into the suggestion process.
    return null;
  }

  const { niche } = referendum;

  const auctionLink = (
    <Link to={WebRoute.Auctions}>
      <FormattedMessage {...NicheReviewStepMessages.WhatNextAuctionLink}/>
    </Link>
  );

  const pageDescription = (
    <Heading size={3} color={themeTypography.textColor} weight={300}>
      <FormattedMessage {...NicheReviewStepMessages.PageHeaderDescription}/>
    </Heading>
  );
  const referendumOid = referendum.oid;
  const approvalDetailsPath = generatePath(WebRoute.ApprovalDetails, {referendumOid});

  return (
    <StepWrapper
      title={<FormattedMessage {...NicheReviewStepMessages.PageHeaderTitle}/>}
      description={pageDescription}
      centerDescription={true}
    >
      <Row gutter={16}>
        <Col
          sm={{span: 24}} md={{span: 20, offset: 2}} lg={{span: 18, offset: 3}}
          style={{marginBottom: 30, marginTop: 30}}
        >
          <NicheCard cover={<NicheCardProgressBar percent={100} strokeColor={themeColors.secondaryBlue}/>}>
            <NicheCardUser user={currentUser} targetBlank={true}/>

            <ChannelCardTitleAndDesc
              channel={niche as Niche}
              center={true}
              linkPath={approvalDetailsPath}
            />
          </NicheCard>
        </Col>
      </Row>

      <Row gutter={16}>
        <Col span={24}>
          <Heading size={3} color={themeTypography.textColor} weight={300}>
            <FormattedMessage {...NicheReviewStepMessages.WhatNextTitle}/>
          </Heading>

          <ParagraphWrapper>
            <Paragraph color="light">
              <FormattedMessage {...NicheReviewStepMessages.WhatNextParagraph} values={{auctionLink}}/>
            </Paragraph>
          </ParagraphWrapper>
        </Col>
      </Row>

      <Row gutter={16} type="flex" justify="center" style={{marginTop: 50}}>
        <Button
          type="primary"
          size="large"
          style={{minWidth: 200}}
          href={approvalDetailsPath}
        >
          <FormattedMessage {...NicheReviewStepMessages.BtnText}/>
        </Button>
      </Row>
    </StepWrapper>
  );
};
