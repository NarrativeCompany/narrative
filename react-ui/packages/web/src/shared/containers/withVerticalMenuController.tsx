import * as React from 'react';
import { compose, withProps } from 'recompose';
import {
  TabDetails,
  TabsControllerParentProps,
  visibleTabsFilter,
  WithTabsControllerProps
} from './withTabsController';
import {
  NarrowViewportMenuControllerProps,
  withNarrowViewportMenuController
} from './withNarrowViewportMenuController';
import { Menu } from '../components/Menu';
import MenuItem from 'antd/lib/menu/MenuItem';
import { NavLink } from '../components/NavLink';
import { InjectedIntlProps, injectIntl } from 'react-intl';
import styled from '../styled';
import { mediaQuery } from '../styled/utils/mediaQuery';

const MenuContainer = styled.div`
  ${mediaQuery.hide_sm_down};
`;

export interface WithVerticalMenuControllerProps extends WithTabsControllerProps, NarrowViewportMenuControllerProps {
  verticalMenu: React.ReactNode;
}

type ParentProps = WithTabsControllerProps &
  TabsControllerParentProps &
  NarrowViewportMenuControllerProps;

type WithProps = ParentProps &
  InjectedIntlProps;

export const withVerticalMenuController = compose(
  withNarrowViewportMenuController,
  injectIntl,
  withProps<Pick<WithVerticalMenuControllerProps, 'verticalMenu'>, WithProps>(
    (props): Pick<WithVerticalMenuControllerProps, 'verticalMenu'> => {
      const { activeTab, tabs, intl: { formatMessage } } = props;

      const verticalMenu = (
        <MenuContainer>
          <Menu mode="vertical">
            {tabs.filter(visibleTabsFilter).map((tab: TabDetails) => {
              if (!tab.path) {
                // todo:error-handler: We should always have a path set by withMenuController.
                return null;
              }
              const { title, path } = tab;

              return (
                <MenuItem key={path}>
                  <NavLink to={path} isActive={() => tab === activeTab}>
                    {formatMessage(title)}
                  </NavLink>
                </MenuItem>
              );
           })}
          </Menu>
        </MenuContainer>
      );

      return { verticalMenu };
    }
  )
);
