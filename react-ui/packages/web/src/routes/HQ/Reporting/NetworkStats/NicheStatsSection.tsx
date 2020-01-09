import * as React from 'react';
import { StatsOverviewProps } from './NetworkStatsSections';
import { NetworkStatsMessages } from '../../../../shared/i18n/NetworkStatsMessages';
import { FormattedMessage } from 'react-intl';
import { Col, Row } from 'antd';
import { StatsCard } from './StatsCard';
import { LocalizedNumber } from '../../../../shared/components/LocalizedNumber';
import { StatsSection } from './StatsSection';

export const NicheStatsSection: React.SFC<StatsOverviewProps> = (props) => {
  const { statsOverview } = props;

  return (
    <StatsSection title={<FormattedMessage {...NetworkStatsMessages.Niches} />}>
      <Row gutter={16}>
        <Col md={8}>
          <StatsCard
            highlightColor="primary-blue"
            stat={<LocalizedNumber value={statsOverview.activeNiches} />}
            description={<FormattedMessage {...NetworkStatsMessages.ActiveNiches} />}
          />
        </Col>
        <Col md={8}>
          <StatsCard
            highlightColor="primary-blue"
            stat={<LocalizedNumber value={statsOverview.approvedNiches} />}
            description={<FormattedMessage {...NetworkStatsMessages.ApprovedNiches} />}
          />
        </Col>
        <Col md={8}>
          <StatsCard
            highlightColor="primary-blue"
            stat={<LocalizedNumber value={statsOverview.nicheOwners} />}
            description={<FormattedMessage {...NetworkStatsMessages.NicheOwners} />}
          />
        </Col>
      </Row>
    </StatsSection>
  );
};
