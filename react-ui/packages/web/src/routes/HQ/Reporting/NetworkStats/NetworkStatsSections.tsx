import * as React from 'react';
import { branch, compose, renderComponent } from 'recompose';
import {
  StatsOverview,
  withStatsOverview
} from '@narrative/shared';
import { LoadingProps } from '../../../../shared/utils/withLoadingPlaceholder';
import { Card } from '../../../../shared/components/Card';
import { Col, Row } from 'antd';
import { Paragraph } from '../../../../shared/components/Paragraph';
import { FormattedMessage } from 'react-intl';
import { NetworkStatsMessages } from '../../../../shared/i18n/NetworkStatsMessages';
import { LocalizedTime } from '../../../../shared/components/LocalizedTime';
import { MemberStatsSection } from './MemberStatsSection';
import { RewardsStatsSection } from './RewardsStatsSection';
import { NicheStatsSection } from './NicheStatsSection';
import { PostsStatsSection } from './PostsStatsSection';

export interface StatsOverviewProps {
  statsOverview: StatsOverview;
}

const NetworkStatsSectionsComponent: React.SFC<StatsOverviewProps> = (props) => {
  const { statsOverview } = props;

  const dataGenerationDatetime = <LocalizedTime time={statsOverview.timestamp} fromNow={true} />;

  return (
    <React.Fragment>
      <Row gutter={23}>
        <Col lg={18}>
          <MemberStatsSection {...props} />
        </Col>
      </Row>

      <Row gutter={23}>
        <Col lg={18}>
          <RewardsStatsSection {...props} />
        </Col>
      </Row>

      <Row gutter={23}>
        <Col lg={18}>
          <NicheStatsSection {...props} />
        </Col>
      </Row>

      {statsOverview.topNiches && statsOverview.topNiches.length === 10 &&
        <Row gutter={23}>
          <Col lg={18}>
            <PostsStatsSection {...props} />
          </Col>
        </Row>
      }

      <Paragraph size="small">
        <FormattedMessage {...NetworkStatsMessages.DataLastUpdated} values={{dataGenerationDatetime}} />
      </Paragraph>
    </React.Fragment>
  );
};

export const NetworkStatsSections = compose(
  withStatsOverview,
  branch((props: LoadingProps) => props.loading,
    renderComponent(() => <Card loading={true} />)
  ),
)(NetworkStatsSectionsComponent) as React.ComponentClass<{}>;
