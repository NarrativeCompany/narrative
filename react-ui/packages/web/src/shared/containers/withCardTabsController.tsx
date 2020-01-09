import * as React from 'react';
import { compose, withProps } from 'recompose';
import { CardProps, CardTabListType } from 'antd/lib/card';
import { TabLink } from '../components/Tabs';
import {
  TabDetails,
  TabsControllerParentProps, visibleTabsFilter,
  WithTabsControllerProps
} from './withTabsController';
import {
  withNarrowViewportMenuController,
  WithNarrowViewportMenuControllerProps
} from './withNarrowViewportMenuController';
import { injectGlobal } from '../styled';
import { mediaQuery } from '../styled/utils/mediaQuery';

const cardClassName = 'card-with-header-menu';

const injectSuppressMenuCss = () => injectGlobal`
  .${cardClassName} .ant-card-head {
    ${mediaQuery.hide_sm_down};
  }
`;

export interface CardTabsControllerStyleProps {
  useNarrowTabs?: boolean;
}

export interface WithCardTabsControllerProps extends WithNarrowViewportMenuControllerProps {
  cardTabProps: Pick<CardProps, 'activeTabKey' | 'defaultActiveTabKey' | 'tabList' | 'className'>;
}

type ParentProps = CardTabsControllerStyleProps &
  WithTabsControllerProps &
  TabsControllerParentProps;

export const withCardTabsController = compose(
  withNarrowViewportMenuController,
  withProps<Pick<WithCardTabsControllerProps, 'cardTabProps'>, ParentProps>(
    (props): Pick<WithCardTabsControllerProps, 'cardTabProps'> => {
      const { activeTab, tabs, useNarrowTabs } = props;

      const tabList: CardTabListType[] = [];
      // jw: let's process each visible tab and generate the tabs for the card.
      tabs.filter(visibleTabsFilter).forEach((tab: TabDetails) => {
        if (!tab.path) {
          // todo:error-handler: We should always have a path set by withTabsController.
          return;
        }
        // jw: note: the TabLink does not support noFollow (rel="nofollow")
        tabList.push({
          key: tab.path,
          tab: (
            <TabLink
              route={tab.path}
              title={tab.title}
              isActive={() => tab === activeTab}
              isNarrow={useNarrowTabs}
            />
          )
        });
      });

      // jw: let's ensure that the header menu on the card will be suppressed
      injectSuppressMenuCss();

      return {
        cardTabProps: {
          className: cardClassName,
          activeTabKey: activeTab.path,
          defaultActiveTabKey: activeTab.path,
          tabList
        }
      };
    }
  )
);
