import * as React from 'react';
import { compose, withHandlers, withProps } from 'recompose';
import { NicheRewardsMessages } from '../../../../../shared/i18n/NicheRewardsMessages';
import { SEO } from '../../../../../shared/components/SEO';
import { InjectedIntlProps, injectIntl } from 'react-intl';
import { NicheDetailsConnect, WithNicheDetailsContextProps } from '../../components/NicheDetailsContext';
import { RouteComponentProps, withRouter } from 'react-router';
import { getQueryArg } from '@narrative/shared';
import { SelectValue } from 'antd/lib/select';
import { createUrl } from '../../../../../shared/utils/routeUtils';
import { WebRoute } from '../../../../../shared/constants/routes';
import { RewardPeriodSelector } from '../../../../../shared/components/rewards/RewardPeriodSelector';
import { NicheRewardsHeader } from './NicheRewardsHeader';
import { NicheRewardsTable } from './NicheRewardsTable';
import { Col, Row } from 'antd';
import NicheRewardsPostLeaderboard from './NicheRewardsPostLeaderboard';
import NicheRewardsCreatorLeaderboard from './NicheRewardsCreatorLeaderboard';
import { RewardPeriodsConnect, WithRewardPeriodsProps } from '../../../../../shared/context/RewardPeriodsContext';
import { getChannelUrl } from '../../../../../shared/utils/channelUtils';

export const monthParam = 'month';

type Props =
  InjectedIntlProps &
  WithNicheDetailsContextProps &
  WithRewardPeriodsProps & {
  month: string;
  onMonthChange: (value: SelectValue) => void;
};

const NicheRewardsOverviewComponent: React.SFC<Props> = (props) => {
  const { nicheDetail: { niche }, onMonthChange, rewardPeriods, intl: { formatMessage } } = props;
  const nicheOid = niche.oid;
  const nicheName = niche.name;

  const month = props.month && rewardPeriods.find(rp => rp.yearMonth === props.month) ?
    props.month :
    rewardPeriods[0].yearMonth;

  return (
    <React.Fragment>
      <SEO title={formatMessage(NicheRewardsMessages.OverviewSeoTitle, {nicheName})} />
      <NicheRewardsHeader nicheOid={nicheOid}/>
      <RewardPeriodSelector
        loading={false}
        rewardPeriods={rewardPeriods}
        month={month}
        onChange={onMonthChange}
        includeExcludedMonthsNote={true}
      />
      <NicheRewardsTable nicheOid={nicheOid} month={month}/>
      <Row gutter={16}>
        <Col md={12}>
          <NicheRewardsPostLeaderboard nicheOid={nicheOid} month={month}/>
        </Col>
        <Col md={12}>
          <NicheRewardsCreatorLeaderboard nicheOid={nicheOid} month={month}/>
        </Col>
      </Row>
    </React.Fragment>
  );
};

export default compose(
  NicheDetailsConnect,
  RewardPeriodsConnect,
  injectIntl,
  withRouter,
  withProps((props: RouteComponentProps<{}>) => {
    const { location: { search } } = props;

    const month = getQueryArg(search, monthParam);

    return { month };
  }),
  withHandlers({
    onMonthChange: (
      props: Props & RouteComponentProps<{}>
    ) => (value: SelectValue) => {
      const { nicheDetail: { niche } } = props;

      const newURL = createUrl(
          getChannelUrl(niche, WebRoute.NicheRewards),
          { [monthParam]: value }
        );

      props.history.push(newURL);
    }
  }),
)(NicheRewardsOverviewComponent) as React.ComponentClass<{}>;
