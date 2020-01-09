import * as React from 'react';
import { StatsOverviewProps } from './NetworkStatsSections';
import { NetworkStatsMessages } from '../../../../shared/i18n/NetworkStatsMessages';
import { FormattedMessage } from 'react-intl';
import { StatsCard } from './StatsCard';
import { StatsSection } from './StatsSection';
import { Col, Row } from 'antd';
import { WebRoute } from '../../../../shared/constants/routes';
import { Link } from '../../../../shared/components/Link';
import { NrveValue } from '../../../../shared/components/rewards/NrveValue';

export const RewardsStatsSection: React.SFC<StatsOverviewProps> = (props) => {
  const { statsOverview } = props;

  const details = (
    <Link to={WebRoute.NetworkStatsRewards}>
      <FormattedMessage {...NetworkStatsMessages.Details} />
    </Link>
  );

  return (
    <StatsSection title={<FormattedMessage {...NetworkStatsMessages.Rewards} />}>
      <Row gutter={16}>
        <Col md={12}>
          <StatsCard
            highlightColor="gold"
            stat={<NrveValue value={statsOverview.networkRewardsPaidLastMonth}/>}
            description={<FormattedMessage {...NetworkStatsMessages.PointsPaidOutLastMonth} values={{details}} />}
          />
        </Col>
        <Col md={12}>
          <StatsCard
            highlightColor="gold"
            stat={<NrveValue value={statsOverview.allTimeReferralRewards}/>}
            description={<FormattedMessage {...NetworkStatsMessages.AllTimeReferralRewardPointsPaid} />}
          />
        </Col>
      </Row>
    </StatsSection>
  );
};
