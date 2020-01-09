import * as React from 'react';
import { compose } from 'recompose';
import { Dropdown, Menu } from 'antd';
import { FormattedMessage } from 'react-intl';
import { Link } from '../Link';
import { SharedComponentMessages } from '../../i18n/SharedComponentMessages';
import { RevokeReasonMessages } from '../../i18n/RevokeReasonMessages';
import { PermissionErrorModal } from '../PermissionErrorModal';
import { WithOpenDeletePostConfirmationHandler } from '../../containers/withDeletePostController';
import styled from 'styled-components';
import { Icon } from '../Icon';
import {
  withPermissionsModalHelpers,
  WithPermissionsModalHelpersProps
} from '../../containers/withPermissionsModalHelpers';
import { WithExtractedCurrentUserProps } from '../../containers/withExtractedCurrentUser';
import { PostLink } from './PostLink';
import { Post, PublicationStatus } from '@narrative/shared';

// jw: normally I would type the properties accurately, but antd does not export MenuItemProps.
//     Thankfully, we do not need to use any of the props, so whatever!
const RedMenuItem = styled<{}>((props) => <Menu.Item {...props}/>)`
  &.ant-dropdown-menu-item a {
    color: ${props => props.theme.primaryRed};
  }
`;

export const MenuItemsDivider = styled.hr`
  color: ${p => p.theme.borderGrey};
`;

type ParentProps =
  WithOpenDeletePostConfirmationHandler & {
    post: Post;
    editableByCurrentUser: boolean;
    deletableByCurrentUser: boolean;
    extraMenuItems?: React.ReactNode[];
    useSvgIcon?: boolean;
  };

type Props =
  ParentProps &
  WithPermissionsModalHelpersProps &
  WithExtractedCurrentUserProps;

const PostDropdownComponent: React.SFC<Props> = (props) => {
  const {
    post,
    editableByCurrentUser,
    deletableByCurrentUser,
    handleOpenDeletePostConfirmation,
    extraMenuItems,
    useSvgIcon,
    permissionErrorModalProps,
    permissionLinkSecurer,
    currentUser
  } = props;

  // jw: there is no reason to include this if there is no current user.
  if (!currentUser) {
    return null;
  }

  // jw: let's gather all menu items into here
  let menuItems: React.ReactNode[] = [];

  // jw: never allow editing of posts that are associated with non-active publications
  const editingBlockedByPublication = post.publishedToPublication &&
    post.publishedToPublication.status !== PublicationStatus.ACTIVE;

  // jw: do we have any author controls to add?
  if (editableByCurrentUser && !editingBlockedByPublication) {
    menuItems.push(
      <Menu.Item key="editMenuItem">
        <PostLink post={post} isEditLink={true} linkSecurer={permissionLinkSecurer}>
          <FormattedMessage {...SharedComponentMessages.Edit}/>
        </PostLink>
      </Menu.Item>
    );
  }

  // jw: only include the delete menu item if the post is live, otherwise remove from publication should handle it.
  if (deletableByCurrentUser && post.postLive && handleOpenDeletePostConfirmation) {
    menuItems.push(
      <RedMenuItem key="authorDeleteMenuItem">
        <Link.Anchor onClick={() => handleOpenDeletePostConfirmation(post)}>
          <FormattedMessage {...SharedComponentMessages.Delete}/>
        </Link.Anchor>
      </RedMenuItem>
    );
  }

  // jw: let's place any extra items above the author tools.
  if (extraMenuItems && extraMenuItems.length) {
    // jw: if we already have options, let's add a horizontal ruler between the author tools and the extra items
    if (menuItems.length) {
      menuItems.push(<MenuItemsDivider key="editDeleteToolsDivider" />);
    }

    menuItems = menuItems.concat(extraMenuItems);
  }

  // jw: finally, if we do not have any menu items, short out.
  if (!menuItems.length) {
    return null;
  }

  const menu = (
    <Menu style={{ minWidth: 125 }}>
      {menuItems.map((item) => item)}
    </Menu>
  );

  return (
    <React.Fragment>
      <Dropdown overlay={menu} placement="bottomRight" trigger={['click', 'hover']}>
        <Icon
          svgIcon={useSvgIcon ? 'ellipsis' : undefined}
          type={useSvgIcon ? undefined : 'ellipsis'}
          style={{ fontSize: 25 }}
        />
      </Dropdown>

      {permissionErrorModalProps && <PermissionErrorModal {...permissionErrorModalProps} />}
    </React.Fragment>
  );
};

export const PostDropdown = compose(
  withPermissionsModalHelpers('postContent', RevokeReasonMessages.EditPosts)
)(PostDropdownComponent) as React.ComponentClass<ParentProps>;
