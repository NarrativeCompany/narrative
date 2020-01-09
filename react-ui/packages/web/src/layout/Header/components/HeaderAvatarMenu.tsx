import * as React from 'react';
import { compose, withHandlers } from 'recompose';
import { Link } from '../../../shared/components/Link';
import { Dropdown, Menu } from 'antd';
import { ClickParam, MenuProps } from 'antd/lib/menu';
import { WebRoute } from '../../../shared/constants/routes';
import { FormattedMessage } from 'react-intl';
import { HeaderAvatarMenuMessages } from '../../../shared/i18n/HeaderAvatarMenuMessages';
import { logout } from '../../../shared/utils/authTokenUtils';
import { User, withUpdateAuthState, WithUpdateAuthStateProps } from '@narrative/shared';
import { generatePath } from 'react-router';
import styled from '../../../shared/styled';

// tslint:disable-next-line no-any
const AvatarMenu = styled<MenuProps & {theme: any}>(({theme, ...rest}) => <Menu {...rest}/>)`
  .ant-dropdown-menu-item {
    padding: 12px 20px;
    min-width: 200px;
  }
`;

interface WithHandlers {
  // tslint:disable-next-line no-any
  handleMenuItemSelect: (param: ClickParam) => any;
}

interface ParentProps {
  currentUser: User;
}

type Props =
  ParentProps &
  WithUpdateAuthStateProps &
  WithHandlers;

export const HeaderAvatarMenuComponent: React.SFC<Props> = (props) => {
  const { handleMenuItemSelect, currentUser } = props;

  const { username } = currentUser;

  const menu = (
    <AvatarMenu onClick={handleMenuItemSelect}>
      <Menu.Item key="yourProfile">
        <Link to={generatePath(WebRoute.UserProfile, {username})}>
          <FormattedMessage {...HeaderAvatarMenuMessages.YourProfileMenuItem}/>
        </Link>
      </Menu.Item>
      <Menu.Item key="manageAccount">
        <Link to={WebRoute.MemberCP}>
          <FormattedMessage {...HeaderAvatarMenuMessages.ManageAccountMenuItem}/>
        </Link>
      </Menu.Item>
      <Menu.Item key="signOut">
        <Link.Anchor>
          <FormattedMessage {...HeaderAvatarMenuMessages.SignOutMenuItem}/>
        </Link.Anchor>
      </Menu.Item>
    </AvatarMenu>
  );

  return (
    <Dropdown overlay={menu} placement="bottomRight" trigger={['click', 'hover']}>
      {props.children}
    </Dropdown>
  );
};

export const HeaderAvatarMenu = compose(
  withUpdateAuthState,
  withHandlers({
    handleMenuItemSelect: () => async (param: ClickParam) => {
      if (param.key !== 'signOut') {
        return;
      }

      await logout();
    }
  })
)(HeaderAvatarMenuComponent) as React.ComponentClass<ParentProps>;
