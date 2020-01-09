import * as React from 'react';
import { Tabs as AntTabs } from 'antd';
import { TabsProps } from 'antd/lib/tabs';
import { NavLink, NavLinkProps } from './NavLink';
import { FormattedMessage } from 'react-intl';
import { match } from 'react-router-dom';
import { History } from 'history';
import styled, { css } from '../../shared/styled';
import * as H from 'history';
import { mediaQuery } from '../styled/utils/mediaQuery';
import { Icon } from './Icon';

/**
 * Tab titles are bound to TabPanes from Tabs children.
 * TabPane is not the master of dealing with the Tab titles.
 * As for now, TabPanes cannot work alone, they need Tabs.
 * And Tabs cannot handle custom components because its direct children need to have tab and key props.
 * To work around this we have TabLink component that can be passed to TabPane tab prop if an anchor is required
 * Otherwise you can pass any React.ReactNode to TabPane tab prop (if anchor isn't required)
 * See Auctions.tsx for example
 */

// Tabs component
// Wrapper and controller for TabPane
export const Tabs = styled<TabsProps>(AntTabs)`
  // This style override applies our primary blue to the colored border that accompanies an active tab
  .ant-tabs-ink-bar {
    background-color: ${p => p.theme.primaryBlue};
  }
  
  // remove padding and margin from the element that wraps our Link so the Link has 100% width/height of the tab
  .ant-tabs-nav .ant-tabs-tab {
    margin: 0 !important;
    padding: 0 !important;
  }
`;

// TabPane component
// export required Tabs child component here so all 3 components can be imported from the same place
export const TabPane = AntTabs.TabPane;

// TabLink component
interface TabLinkStyleProps {
  isNarrow?: boolean;
}

type StyledTabLinkProps = NavLinkProps & TabLinkStyleProps;

function getTabPadding(props: TabLinkStyleProps) {
  if (props.isNarrow) {
    return css`padding: 12px 24px;`;
  }

  return css`padding: 12px 32px;`;
}

// Needs to be passed to the tab prop of the TabPane component if an anchor is required
const StyledTabLink = styled<StyledTabLinkProps>(({isNarrow, ...props}) => <NavLink {...props} />)`
  display: inline-block;
  ${p => getTabPadding(p)}
`;

const XsContainer = styled.span`
  ${mediaQuery.hide_sm_up}
`;

const SmallUpContainer = styled.span`
  ${mediaQuery.hide_xs}
`;

interface TabLinkProps extends TabLinkStyleProps {
  route: History.LocationDescriptor;
  title: FormattedMessage.MessageDescriptor;
  xsAntIconReplacement?: string;
  isActive?<P>(match: match<P>, location: H.Location): boolean;
}

export const TabLink: React.SFC<TabLinkProps> = (props) => {
  const { title, route, xsAntIconReplacement, ...linkProps } = props;

  if (xsAntIconReplacement) {
    return (
      <StyledTabLink to={route} {...linkProps}>
        <XsContainer>
          <Icon type={xsAntIconReplacement} />
        </XsContainer>
        <SmallUpContainer>
          <FormattedMessage {...title}/>
        </SmallUpContainer>
      </StyledTabLink>
    );
  }

  return (
    <StyledTabLink to={route} {...linkProps}>
      <FormattedMessage {...title}/>
    </StyledTabLink>
  );
};
