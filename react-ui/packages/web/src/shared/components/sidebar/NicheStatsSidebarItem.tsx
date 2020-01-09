import * as React from 'react';
import { compose } from 'recompose';
import { withNicheStats } from '@narrative/shared';
import { ContainedLoading } from '../Loading';
import { SectionHeader } from '../SectionHeader';
import { FormattedMessage } from 'react-intl';
import { SidebarMessages } from '../../i18n/SidebarMessages';
import { Card } from '../Card';
import { CardSpacingContainer } from './TrendingNichesSidebarItem';
import { Link } from '../Link';
import { WebRoute } from '../../constants/routes';
import * as H from 'history';
import { Heading } from '../Heading';
import { Paragraph } from '../Paragraph';
import { LocalizedNumber } from '../LocalizedNumber';
import { WithExtractedNicheStatsProps } from '@narrative/shared';

interface NicheStatCardProps {
  stat: number;
  description: FormattedMessage.MessageDescriptor;
  to: H.LocationDescriptor;
}

const NicheStatCard: React.SFC<NicheStatCardProps> = (props) => {
  const { stat, description, to } = props;

  return (
    <Card>
      <Heading size={2} style={{textAlign: 'center'}}>
        <LocalizedNumber value={stat} />
      </Heading>
      <Paragraph style={{textAlign: 'center'}}>
        <Link to={to}>
          <FormattedMessage {...description} values={{stat}}/>
        </Link>
      </Paragraph>
    </Card>
  );
};

const NicheStatCardsComponent: React.SFC<WithExtractedNicheStatsProps> = (props) => {
  const { loadingNicheStats, nicheStats } = props;

  if (loadingNicheStats) {
    return <ContainedLoading/>;
  }

  return (
    <CardSpacingContainer>
      <NicheStatCard
        stat={nicheStats.nichesAwaitingApproval}
        description={SidebarMessages.NichesWaitingApproval}
        to={WebRoute.Approvals}
      />
      <NicheStatCard
        stat={nicheStats.nichesForSale}
        description={SidebarMessages.NichesForSale}
        to={WebRoute.Auctions}
      />
    </CardSpacingContainer>
  );
};

const NicheStatCards = compose(
  withNicheStats
)(NicheStatCardsComponent) as React.ComponentClass<{}>;

export const NicheStatsSidebarItem: React.SFC<{}> = () => {
  return (
    <React.Fragment>
      <SectionHeader
        title={<FormattedMessage {...SidebarMessages.HqUpdates} />}
        description={<FormattedMessage {...SidebarMessages.HqUpdatesDescription} />}
        extra={<Link to={WebRoute.HQ}><FormattedMessage {...SidebarMessages.VisitHq} /></Link>}
      />
      <NicheStatCards />
    </React.Fragment>
  );
};
