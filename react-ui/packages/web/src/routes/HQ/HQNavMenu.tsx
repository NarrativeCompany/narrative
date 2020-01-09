import * as React from 'react';
import { compose, lifecycle } from 'recompose';
import { Menu } from '../../shared/components/Menu';
import { NavLink } from '../../shared/components/NavLink';
import { withRouter, RouteComponentProps } from 'react-router-dom';
import { Menu as AntMenu } from 'antd';
import { ButtonDropdownMenu } from '../../shared/components/ButtonDropdownMenu';
import { Card } from '../../shared/components/Card';
import { WebRoute } from '../../shared/constants/routes';
import { FormattedMessage } from 'react-intl';
import { HQMessages } from '../../shared/i18n/HQMessages';
import { withState, WithStateProps } from '@narrative/shared';
import { SelectParam } from 'antd/lib/menu';
import styled from '../../shared/styled';

const MenuItemGroup = AntMenu.ItemGroup;
const MenuItem = AntMenu.Item;

const menuItemKeys = {
  [WebRoute.Approvals]: <FormattedMessage {...HQMessages.MenuItemApprovals}/>,
  [WebRoute.Auctions]: <FormattedMessage {...HQMessages.MenuItemBid}/>,
  [WebRoute.LeadershipTribunal]: <FormattedMessage {...HQMessages.MenuItemTribunalMembers}/>,
  [WebRoute.Appeals]: <FormattedMessage {...HQMessages.MenuItemAppeals}/>,
  [WebRoute.Moderators]: <FormattedMessage {...HQMessages.MenuItemModerators}/>,
  [WebRoute.NetworkStatsRewards]: <FormattedMessage {...HQMessages.MenuItemRewards}/>,
  [WebRoute.NetworkStats]: <FormattedMessage {...HQMessages.MenuItemNetworkStats}/>
};

const DropdownMenuWrapper = styled.div`
  margin-bottom: 30px;
  
  @media screen and (min-width: 992px) {
    display: none;
  }
`;

const SideNavWrapper = styled.div`
  @media screen and (max-width: 991px) {
    display: none;
  }
`;

interface State {
  selectedMenuItem: string;
}

const initialState: State = {
  selectedMenuItem: WebRoute.Approvals,
};

type Props =
  WithStateProps<State> &
  RouteComponentProps<{}>;

type MenuItemKeyType = keyof typeof menuItemKeys;

// tslint:disable-next-line no-any
function getHQNavMenuItem(type: MenuItemKeyType): any {
  return (
    <MenuItem key={type}>
      <NavLink to={type}>
        {menuItemKeys[type]}
      </NavLink>
    </MenuItem>
  );
}

export const HQNavMenuComponent: React.SFC<Props> = (props) => {
  const { state, setState } = props;

  const NicheMenuItems = [
    (
      getHQNavMenuItem(WebRoute.Approvals)
    ),
    (
      getHQNavMenuItem(WebRoute.Auctions)
    )
  ];

  const TribunalMenuItems = [
    (
      getHQNavMenuItem(WebRoute.LeadershipTribunal)
    ),
    (
      getHQNavMenuItem(WebRoute.Appeals)
    )
  ];

  const ModeratorMenuItems = [
    (
      getHQNavMenuItem(WebRoute.Moderators)
    )
  ];

  const NetworkStatsMenuItems = [
    (
      getHQNavMenuItem(WebRoute.NetworkStats)
    ),
    (
      getHQNavMenuItem(WebRoute.NetworkStatsRewards)
    )
  ];

  const DropdownMenu = (
    <Menu onClick={(params: SelectParam) => setState(ss => ({...ss, selectedMenuItem: params.key}))}>
      {...NicheMenuItems}
      {...ModeratorMenuItems}
      {...TribunalMenuItems}
      {...NetworkStatsMenuItems}
    </Menu>
  );

  const SideNavMenu = (
    <Card style={{maxWidth: 125, marginRight: 25}} bodyStyle={{padding: 20}}>
      <Menu>
        <MenuItemGroup key="one" title={<FormattedMessage {...HQMessages.MenuGroupNicheTitle}/>}>
          {...NicheMenuItems}
        </MenuItemGroup>
        <MenuItemGroup key="two" title={<FormattedMessage {...HQMessages.MenuGroupModeratorsTitle}/>}>
          {...ModeratorMenuItems}
        </MenuItemGroup>
        <MenuItemGroup key="three" title={<FormattedMessage {...HQMessages.MenuGroupLeadershipTitle}/>}>
          {...TribunalMenuItems}
        </MenuItemGroup>
        <MenuItemGroup key="four" title={<FormattedMessage {...HQMessages.MenuGroupNetworkStatsTitle}/>}>
          {...NetworkStatsMenuItems}
        </MenuItemGroup>
      </Menu>
    </Card>
  );

  return (
    <React.Fragment>
      <DropdownMenuWrapper>
        <ButtonDropdownMenu
          overlay={DropdownMenu}
          btnText={menuItemKeys[state.selectedMenuItem]}
          isFullWidth={true}
        />
      </DropdownMenuWrapper>

      <SideNavWrapper>
        {SideNavMenu}
      </SideNavWrapper>
    </React.Fragment>
  );
};

export const HQNavMenu = compose(
  withRouter,
  withState<State>(initialState),
  lifecycle<Props, {}>({
    // tslint:disable-next-line object-literal-shorthand
    componentDidMount: function () {
      const { location, setState } = this.props;
      const defaultMenuItem = Object.keys(menuItemKeys).find(key => location.pathname.includes(key));

      if (!defaultMenuItem) {
        return;
      }

      setState(ss => ({...ss, selectedMenuItem: defaultMenuItem}));
    }
  })
)(HQNavMenuComponent) as React.ComponentClass<{}>;
