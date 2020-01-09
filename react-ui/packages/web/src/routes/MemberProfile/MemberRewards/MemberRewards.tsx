import * as React from 'react';
import { compose, withHandlers, withProps } from 'recompose';
import { MemberProfileConnect, WithMemberProfileProps } from '../../../shared/context/MemberProfileContext';
import { TabDetails, TabRoute } from '../../../shared/containers/withTabsController';
import { WebRoute } from '../../../shared/constants/routes';
import * as Routes from '../../index';
import { withPillMenuController, WithPillMenuControllerProps } from '../../../shared/containers/withPillMenuController';
import { PillMenu } from '../../../shared/components/navigation/pills/PillMenu';
import { MemberRewardsMessages } from '../../../shared/i18n/MemberRewardsMessages';
import { FormattedMessage, InjectedIntlProps, injectIntl } from 'react-intl';
import { WithExtractedRewardPeriodsProps, withState, WithStateProps, withUserRewardPeriods } from '@narrative/shared';
import { Block } from '../../../shared/components/Block';
import { SEO } from '../../../shared/components/SEO';
import { Link } from '../../../shared/components/Link';
import {
  UnderstandingNarrativePointsSection
} from '../../../shared/components/rewards/UnderstandingNarrativePointsSection';
import { fullPlaceholder, withLoadingPlaceholder } from '../../../shared/utils/withLoadingPlaceholder';
import { RewardPeriodsContext } from '../../../shared/context/RewardPeriodsContext';
import {
  MemberProfileRewardsHeader
} from './components/MemberProfileRewardsHeader';

import { MemberProfileCurrentUserRewardsHeader } from './components/MemberProfileCurrentUserRewardsHeader';

interface State {
  renderCycle: number;
}

export interface MemberRewardsTransactionsChangedHandler {
  onTransactionsChanged: () => void;
}

type Props =
  InjectedIntlProps &
  WithMemberProfileProps &
  WithPillMenuControllerProps &
  WithExtractedRewardPeriodsProps &
  MemberRewardsTransactionsChangedHandler &
  WithStateProps<State>;

const MemberRewardsComponent: React.SFC<Props> = (props) => {
  const {
    pillMenuProps,
    selectedTabRoute,
    rewardPeriods,
    isForCurrentUser,
    onTransactionsChanged,
    state: { renderCycle },
    intl: { formatMessage },
    detailsForProfile: { user: { displayName } }
  } = props;

  // bl: if the User doesn't have any reward periods, then show a message since there's nothing to display yet
  if (!rewardPeriods.length) {
    const narrativeRewardsLink = <Link.About type="rewards"/>;
    return (
      <React.Fragment>
        <SEO title={formatMessage(MemberRewardsMessages.OverviewSeoTitle, {displayName})} />
        <Block size="large">
          <FormattedMessage {...MemberRewardsMessages.NoRewards} values={{narrativeRewardsLink}}/>
        </Block>
      </React.Fragment>
    );
  }

  return (
    <React.Fragment>
      <PillMenu {...pillMenuProps} />

      {isForCurrentUser
        ? <MemberProfileCurrentUserRewardsHeader
          renderCycle={renderCycle}
          onTransactionsChanged={onTransactionsChanged}
        />
        : <MemberProfileRewardsHeader />
      }

      {/*
        jw: To force a reload when the transaction list changes, we need to specify a key here that will change when the
            transactions do.
      */}
      <Block key={renderCycle} style={{marginBottom: 20, borderBottom: '1px solid #dfdfdf'}}>
        <RewardPeriodsContext.Provider value={{rewardPeriods, onCanceledRedemptionRequest: onTransactionsChanged}}>
          {selectedTabRoute}
        </RewardPeriodsContext.Provider>
      </Block>
      <UnderstandingNarrativePointsSection/>

    </React.Fragment>
  );
};

export default compose(
  injectIntl,
  MemberProfileConnect,
  withProps((props: WithMemberProfileProps) => {
    const { detailsForProfile: { user: { username } } } = props;

    const tabs: TabDetails[] = [
      new TabDetails(
        new TabRoute(WebRoute.UserProfileRewards),
        MemberRewardsMessages.Overview,
        Routes.MemberRewardsOverview,
      ),
      new TabDetails(
        new TabRoute(WebRoute.UserProfileRewardsTransactions),
        MemberRewardsMessages.Transactions,
        Routes.MemberRewardsTransactions,
      ),
    ];

    return {
      tabs,
      tabRouteParams: { username }
    };
  }),
  withProps((props: WithMemberProfileProps) => {
    const { detailsForProfile: { user } } = props;
    const userOid = user.oid;

    return { userOid };
  }),
  withUserRewardPeriods,
  withLoadingPlaceholder(fullPlaceholder),
  withPillMenuController,
  withState<State>({renderCycle: 1}),
  withHandlers<Props, MemberRewardsTransactionsChangedHandler>({
    onTransactionsChanged: (props) => () => {
      const { setState, state: { renderCycle } } = props;

      // bl: always update the state for both the Overview and Transactions tabs since they both show the balance.
      setState(ss => ({...ss, renderCycle: renderCycle + 1}));
    }
  })
)(MemberRewardsComponent) as React.ComponentClass<{}>;
