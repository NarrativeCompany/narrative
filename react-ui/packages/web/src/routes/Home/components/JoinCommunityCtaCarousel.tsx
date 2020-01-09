import * as React from 'react';
import { JoinCommunityCta } from './JoinCommunityCta';
import { HomeMessages } from '../../../shared/i18n/HomeMessages';
import styled, { theme } from '../../../shared/styled';
import { Carousel } from 'antd';
import { CarouselProps } from 'antd/lib/carousel';

const StyledCarousel = styled<CarouselProps>(Carousel)`
  margin-bottom: 20px;
`;

export const JoinCommunityCtaCarousel: React.SFC<{}> = () => {
  return (
    <StyledCarousel autoplay={true} autoplaySpeed={5000}>
      <JoinCommunityCta
        message={HomeMessages.ContentCtaMessage}
        iconType="magnifying-glass"
        backgroundColor={theme.darkBlue}
        headlineColor={theme.lightOrange}
        messageColor="white"
      />
      <JoinCommunityCta
        message={HomeMessages.RewardsCtaMessage}
        iconType="nrve-and-usd"
        backgroundColor={theme.lightOrange}
        headlineColor={theme.primaryRed}
        messageColor={theme.mediumGray}
      />
      <JoinCommunityCta
        message={HomeMessages.GovernanceCtaMessage}
        iconType="crown"
        backgroundColor={theme.primaryRed}
        headlineColor={theme.lightOrange}
        messageColor="white"
      />
    </StyledCarousel>
  );
};
