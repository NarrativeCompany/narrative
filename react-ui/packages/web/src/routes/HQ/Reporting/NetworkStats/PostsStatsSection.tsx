import * as React from 'react';
import { StatsOverviewProps } from './NetworkStatsSections';
import { StatsSection } from './StatsSection';
import { FormattedMessage } from 'react-intl';
import { NetworkStatsMessages } from '../../../../shared/i18n/NetworkStatsMessages';
import { Col, Row } from 'antd';
import { StatsCard } from './StatsCard';
import { LocalizedNumber } from '../../../../shared/components/LocalizedNumber';
import { HighlightedCard, HighlightedCardProps } from '../../../../shared/components/HighlightedCard';
import { Paragraph } from '../../../../shared/components/Paragraph';
import styled from '../../../../shared/styled';
import { TopNiche } from '@narrative/shared';
import { TopNicheStats } from './TopNicheStats';

const StyledHighlightedCard = styled<HighlightedCardProps>((props) => <HighlightedCard {...props}/>)`
  & .ant-card-body {
    padding: 5px 24px;
  }
`;

const minHeightStyle = {minHeight: '155px'};

export const PostsStatsSection: React.SFC<StatsOverviewProps> = (props) => {
  const { statsOverview } = props;

  const topNiches = statsOverview.topNiches || [];

  const nicheCount = topNiches.length;
  const splitPoint = Math.ceil(topNiches.length / 2);
  const firstFiveNiches = topNiches.slice(0, splitPoint);
  const lastFiveNiches = topNiches.slice(splitPoint);

  return (
    <StatsSection title={<FormattedMessage {...NetworkStatsMessages.Posts} />}>
      <Row gutter={16}>
        <Col md={8}>
          <StatsCard
            highlightColor="midnight-blue"
            stat={<LocalizedNumber value={statsOverview.totalPosts} />}
            description={<FormattedMessage {...NetworkStatsMessages.TotalPosts} />}
            style={minHeightStyle}
          />
        </Col>
        <Col md={16}>
          <StyledHighlightedCard
            highlightSide="top"
            highlightColor="midnight-blue"
            highlightWidth="wide"
            style={{marginBottom: '15px', ...minHeightStyle}}>
            <Paragraph color="light" style={{textAlign: 'center'}}>
              <FormattedMessage {...NetworkStatsMessages.TopNiches} values={{ nicheCount }} />
            </Paragraph>
            <Row gutter={16}>
              <Col sm={12}>
                {firstFiveNiches.map((topNiche: TopNiche, index: number) => (
                  <TopNicheStats key={index} index={index} topNiche={topNiche} />
                ))}
              </Col>
              <Col sm={12}>
                {lastFiveNiches.map((topNiche: TopNiche, index: number) => (
                  <TopNicheStats key={index + splitPoint} index={index + splitPoint} topNiche={topNiche} />
                ))}
              </Col>
            </Row>
          </StyledHighlightedCard>
        </Col>
      </Row>
    </StatsSection>
  );
};
