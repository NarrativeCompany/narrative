import * as React from 'react';
import { StatsOverviewProps } from './NetworkStatsSections';
import { NetworkStatsMessages } from '../../../../shared/i18n/NetworkStatsMessages';
import { FormattedMessage } from 'react-intl';
import { Col, Row } from 'antd';
import { StatsCard } from './StatsCard';
import { LocalizedNumber } from '../../../../shared/components/LocalizedNumber';
import { StatsSection } from './StatsSection';

export const MemberStatsSection: React.SFC<StatsOverviewProps> = (props) => {
  const { statsOverview } = props;

  return (
    <StatsSection title={<FormattedMessage {...NetworkStatsMessages.Users} />}>
      <Row gutter={16}>
        <Col md={12}>
          <StatsCard
            highlightColor="bright-green"
            stat={<LocalizedNumber value={statsOverview.totalMembers} />}
            description={<FormattedMessage {...NetworkStatsMessages.TotalMembers} />}
          />
        </Col>
        <Col md={12}>
          <StatsCard
            highlightColor="bright-green"
            stat={statsOverview.uniqueVisitorsPast30Days ?
              <LocalizedNumber value={statsOverview.uniqueVisitorsPast30Days} /> :
              <FormattedMessage {...NetworkStatsMessages.NotAvailable} />
            }
            description={<FormattedMessage {...NetworkStatsMessages.UniqueVisitors} />}
          />
        </Col>
      </Row>
    </StatsSection>
  );
};
