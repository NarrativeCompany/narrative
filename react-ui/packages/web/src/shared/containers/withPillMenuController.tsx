import { compose, withProps } from 'recompose';
import {
  TabDetails,
  TabsControllerParentProps,
  visibleTabsFilter,
  withTabsController,
  WithTabsControllerProps
} from './withTabsController';
import { PillMenuProps } from '../components/navigation/pills/PillMenu';
import { PillMenuItemProps } from '../components/navigation/pills/PillMenuItem';
import { CSSProperties } from 'react';

interface ParentProps {
  style?: CSSProperties;
}

interface PillMenuControllerProps {
  pillMenuProps: PillMenuProps;
}

export type WithPillMenuControllerProps = WithTabsControllerProps & PillMenuControllerProps;

export const withPillMenuController = compose<{}, ParentProps>(
  withTabsController,
  withProps((props: WithTabsControllerProps & TabsControllerParentProps & ParentProps):
  PillMenuControllerProps => {
    const { activeTab, tabs, style } = props;

    const pills: PillMenuItemProps[] = [];
    // jw: let's process each tab and
    tabs.filter(visibleTabsFilter).forEach((tab: TabDetails) => {
      if (!tab.path) {
        // todo:error-handler: We should always have a path set by withTabsController.
        return;
      }
      const { title, path, noFollow, icon } = tab;

      pills.push({ title, path, noFollow, icon });
    });

    return {
      pillMenuProps: {
        selectedPath: activeTab.path || '',
        pills,
        style
      }
    };
  })
);
