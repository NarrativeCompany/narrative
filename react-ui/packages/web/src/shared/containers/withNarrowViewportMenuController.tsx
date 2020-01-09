import { compose, withProps } from 'recompose';
import {
  TabDetails,
  TabsControllerParentProps,
  visibleTabsFilter,
  withTabsController,
  WithTabsControllerProps
} from './withTabsController';
import * as React from 'react';
import { Menu } from '../components/Menu';
import { NavLink } from '../components/NavLink';
import MenuItem from 'antd/lib/menu/MenuItem';
import { ButtonDropdownMenu } from '../components/ButtonDropdownMenu';
import { injectIntl } from 'react-intl';
import InjectedIntlProps = ReactIntl.InjectedIntlProps;
import styled from '../styled';
import { mediaQuery } from '../styled/utils/mediaQuery';

// jw: currently, this is only used for medium/small grids, so let's maintain that!
const DropdownMenuWrapper = styled.div`
  margin-bottom: 20px;
  
  ${mediaQuery.hide_md_up};
`;

export interface NarrowViewportMenuControllerProps {
  narrowViewportMenu: React.ReactNode;
}

export type WithNarrowViewportMenuControllerProps = WithTabsControllerProps & NarrowViewportMenuControllerProps;

export const withNarrowViewportMenuController = compose(
  withTabsController,
  injectIntl,
  withProps<NarrowViewportMenuControllerProps, WithTabsControllerProps & TabsControllerParentProps & InjectedIntlProps>(
    (props): NarrowViewportMenuControllerProps => {
      const { activeTab, tabs, intl: { formatMessage } } = props;

      const menuOverlay = (
        <Menu>
          {tabs.filter(visibleTabsFilter).map((tab: TabDetails) => {
            if (!tab.path) {
              // todo:error-handler: We should always have a path set by withTabsController.
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
      );

      const narrowViewportMenu = (
        <DropdownMenuWrapper>
          <ButtonDropdownMenu
            overlay={menuOverlay}
            btnText={formatMessage(activeTab.title)}
            isFullWidth={true}
          />
        </DropdownMenuWrapper>
      );

      return { narrowViewportMenu };
    }
  )
);
