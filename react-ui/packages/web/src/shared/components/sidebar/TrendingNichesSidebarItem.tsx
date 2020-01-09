import * as React from 'react';
import { compose } from 'recompose';
import { Niche, withTrendingNiches, WithTrendingNichesProps } from '@narrative/shared';
import { ContainedLoading } from '../Loading';
import { SectionHeader } from '../SectionHeader';
import { FormattedMessage } from 'react-intl';
import { SidebarMessages } from '../../i18n/SidebarMessages';
import { Card } from '../Card';
import styled from '../../styled';
import { Link } from '../Link';

export const CardSpacingContainer = styled.div`
  margin-bottom: 30px;

  .ant-card:not(:first-child) {
    margin-top: 20px;
  }
`;

const TrendingNicheList: React.SFC<WithTrendingNichesProps> = (props) => {
  const { loadingTrendingNiches, trendingNiches } = props;

  if (loadingTrendingNiches) {
    return <ContainedLoading/>;
  }

  return (
    <CardSpacingContainer>
      {trendingNiches.map((niche: Niche) => <Card.Channel key={niche.oid} channel={niche} />)}
    </CardSpacingContainer>
  );
};

const TrendingNichesSidebarItemComponent: React.SFC<WithTrendingNichesProps> = (props) => {
  const { loadingTrendingNiches, trendingNiches } = props;

  // jw: let's not render anything if there are no trending niches...
  if (!loadingTrendingNiches && !trendingNiches.length) {
    return null;
  }

  return (
    <React.Fragment>
      <SectionHeader
        title={<FormattedMessage {...SidebarMessages.TrendingNiches} />}
        extra={<Link.About type="nicheAbout" />}
      />
      <TrendingNicheList {...props} />
    </React.Fragment>
  );
};

export const TrendingNichesSidebarItem = compose(
  withTrendingNiches
)(TrendingNichesSidebarItemComponent) as React.ComponentClass<{}>;
