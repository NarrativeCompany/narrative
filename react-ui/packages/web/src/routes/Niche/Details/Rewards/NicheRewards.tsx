import * as React from 'react';
import { compose, withProps } from 'recompose';
import { NicheDetailsConnect, WithNicheDetailsContextProps } from '../components/NicheDetailsContext';
import { NicheRewardsMessages } from '../../../../shared/i18n/NicheRewardsMessages';
import { PillMenu } from '../../../../shared/components/navigation/pills/PillMenu';
import {
  withPillMenuController,
  WithPillMenuControllerProps
} from '../../../../shared/containers/withPillMenuController';
import { TabDetails, TabRoute } from '../../../../shared/containers/withTabsController';
import { WebRoute } from '../../../../shared/constants/routes';
import * as Routes from '../../../index';
import { WithExtractedRewardPeriodsProps, withNicheRewardPeriods } from '@narrative/shared';
import { fullPlaceholder, withLoadingPlaceholder } from '../../../../shared/utils/withLoadingPlaceholder';
import { EnhancedNicheStatus } from '../../../../shared/enhancedEnums/nicheStatus';
import { FormattedMessage, InjectedIntlProps, injectIntl } from 'react-intl';
import { Block } from '../../../../shared/components/Block';
import { SEO } from '../../../../shared/components/SEO';
import { Link } from '../../../../shared/components/Link';
import { getIdForUrl } from '../../../../shared/utils/routeUtils';
import {
  UnderstandingNarrativePointsSection
} from '../../../../shared/components/rewards/UnderstandingNarrativePointsSection';
import { RewardPeriodsContext } from '../../../../shared/context/RewardPeriodsContext';

type Props =
  InjectedIntlProps &
  WithNicheDetailsContextProps &
  WithPillMenuControllerProps &
  WithExtractedRewardPeriodsProps;

const NicheRewardsComponent: React.SFC<Props> = (props) => {
  const {
    pillMenuProps,
    selectedTabRoute,
    rewardPeriods,
    intl: { formatMessage },
    nicheDetail: { niche }
  } = props;
  const nicheName = niche.name;
  const nicheStatus = EnhancedNicheStatus.get(niche.status);

  // bl: if the Niche doesn't have any reward periods, then show a message since there's nothing to display yet
  if (!rewardPeriods.length) {
    const narrativeRewardsLink = <Link.About type="rewards"/>;
    let message;
    if (nicheStatus.isActive()) {
      message = <FormattedMessage {...NicheRewardsMessages.NoRewardsYet} values={{narrativeRewardsLink}}/>;
    } else {
      message = <FormattedMessage {...NicheRewardsMessages.NoRewards} values={{narrativeRewardsLink}}/>;
    }
    return (
      <React.Fragment>
        <SEO title={formatMessage(NicheRewardsMessages.OverviewSeoTitle, {nicheName})} />
        <Block size="large">
          {message}
        </Block>
      </React.Fragment>
    );
  }

  return (
    <React.Fragment>
      <PillMenu {...pillMenuProps} />

      <Block style={{marginBottom: 20, borderBottom: '1px solid #dfdfdf', paddingBottom: 20}}>
        <RewardPeriodsContext.Provider value={{rewardPeriods}}>
          {selectedTabRoute}
        </RewardPeriodsContext.Provider>
      </Block>
      <UnderstandingNarrativePointsSection/>

    </React.Fragment>
  );
};

export default compose(
  injectIntl,
  NicheDetailsConnect,
  withProps((props: WithNicheDetailsContextProps) => {
    const { nicheDetail: { niche } } = props;
    const id = getIdForUrl(niche.prettyUrlString, niche.oid);

    const tabs: TabDetails[] = [
      new TabDetails(
        new TabRoute(WebRoute.NicheRewards),
        NicheRewardsMessages.Overview,
        Routes.NicheRewardsOverview
      ),
      new TabDetails(
        new TabRoute(WebRoute.NicheRewardsLeaderboard),
        NicheRewardsMessages.Leaderboard,
        Routes.NicheRewardsLeaderboard
      ),
    ];

    return {
      tabs,
      tabRouteParams: { id }
    };
  }),
  withProps((props: WithNicheDetailsContextProps) => {
    const { nicheDetail: { niche } } = props;
    const nicheOid = niche.oid;

    return { nicheOid };
  }),
  withNicheRewardPeriods,
  withLoadingPlaceholder(fullPlaceholder),
  withPillMenuController,
)(NicheRewardsComponent) as React.ComponentClass<{}>;
