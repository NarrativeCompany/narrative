import * as React from 'react';
import { compose } from 'recompose';
import { NicheRewardsMessages } from '../../../../../shared/i18n/NicheRewardsMessages';
import { SEO } from '../../../../../shared/components/SEO';
import NicheRewardsPostLeaderboard from './NicheRewardsPostLeaderboard';
import { InjectedIntlProps, injectIntl } from 'react-intl';
import { NicheDetailsConnect, WithNicheDetailsContextProps } from '../../components/NicheDetailsContext';
import { Col, Row } from 'antd';
import NicheRewardsCreatorLeaderboard from './NicheRewardsCreatorLeaderboard';
import { FormattedMessage } from 'react-intl';
import { Block } from '../../../../../shared/components/Block';
import { RewardsHeaderWrapper } from '../../../../../shared/components/rewards/RewardsHeader';

type Props =
  InjectedIntlProps &
  WithNicheDetailsContextProps;

const NicheRewardsLeaderboardComponent: React.SFC<Props> = (props) => {
  const { nicheDetail: { niche }, intl: { formatMessage } } = props;
  const nicheName = niche.name;
  const nicheOid = niche.oid;

  return (
    <React.Fragment>
      <SEO title={formatMessage(NicheRewardsMessages.LeaderboardSeoTitle, {nicheName})} />

      <RewardsHeaderWrapper>
        <Block size="large">
          <FormattedMessage {...NicheRewardsMessages.LeaderboardIntro}/>
        </Block>
      </RewardsHeaderWrapper>

      <Row gutter={16}>
        <Col md={12}>
          <NicheRewardsPostLeaderboard nicheOid={nicheOid}/>
        </Col>
        <Col md={12}>
          <NicheRewardsCreatorLeaderboard nicheOid={nicheOid}/>
        </Col>
      </Row>
    </React.Fragment>
  );
};

export default compose(
  NicheDetailsConnect,
  injectIntl
)(NicheRewardsLeaderboardComponent) as React.ComponentClass<{}>;
