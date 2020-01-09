import * as React from 'react';
import { branch, compose, withProps } from 'recompose';
import { TabLink, TabPane, Tabs } from '../components/Tabs';
import {
  TabsControllerParentProps,
  visibleTabsFilter,
  withTabsController,
  WithTabsControllerProps
} from './withTabsController';
import {
  NarrowViewportMenuControllerProps,
  withNarrowViewportMenuController
} from './withNarrowViewportMenuController';
import styled from '../styled';
import { mediaQuery } from '../styled/utils/mediaQuery';

const TabBarContainer = styled.div`
  ${mediaQuery.hide_sm_down};
`;

export interface TabBarControllerStyleProps {
  useNarrowTabs?: boolean;
  // jw: in some cases we do not want to switch to a selector for narrow viewports
  showTabsOnNarrowViewport?: boolean;
}

export interface WithTabBarControllerProps extends WithTabsControllerProps {
  tabBar: React.ReactNode;
}

type ParentProps = WithTabsControllerProps &
  TabBarControllerStyleProps &
  TabsControllerParentProps &
  NarrowViewportMenuControllerProps;

export const withTabBarController = compose(
  // jw: only generate the narrow viewport menu if we are not using a consistent UI
  branch<TabBarControllerStyleProps>(props => !props.showTabsOnNarrowViewport,
    withNarrowViewportMenuController,
    withTabsController
  ),
  withProps<Pick<WithTabBarControllerProps, 'tabBar'>, ParentProps>(
    (props): Pick<WithTabBarControllerProps, 'tabBar'> => {
      const { activeTab, tabs, useNarrowTabs, showTabsOnNarrowViewport, narrowViewportMenu } = props;

      let tabBar = (
        <Tabs defaultActiveKey={activeTab.path} activeKey={activeTab.path}>
          {tabs.filter(visibleTabsFilter).map((tab) =>
            // jw: note: TabPane/TabLink does not support noFollow (rel="nofollow")
            // jw: I know this seems odd, but path should always be defined here, but we still need to check it.
            tab.path && <TabPane
              key={tab.path}
              tab={
                <TabLink
                  route={tab.path}
                  title={tab.title}
                  xsAntIconReplacement={tab.xsAntIconReplacement}
                  isActive={() => tab === activeTab}
                  isNarrow={useNarrowTabs}
                />
              }
            />
          )}
        </Tabs>
      );
      if (!showTabsOnNarrowViewport) {
        tabBar = (
          <React.Fragment>
            <TabBarContainer>
              {tabBar}
            </TabBarContainer>

            {/*
              jw: let's include the narrow viewport menu with the tab bar, so that they will render in the same place
                  and make the life of the consumer simpler
            */}
            {narrowViewportMenu}
          </React.Fragment>
        );
      }

      return { tabBar };
    }
  )
);
