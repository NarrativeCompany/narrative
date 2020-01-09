import * as React from 'react';
import { Menu as AntMenu } from 'antd';
import { ClickParam, MenuMode, SelectParam } from 'antd/lib/menu';
import styled from '../styled';

// tslint:disable-next-line no-any
const StyledMenu = styled<ParentProps>(AntMenu as any)`
  &.ant-menu {
    border-right: none;
  }
  
  .ant-menu-item-group {
    &:not(:last-child) {
      margin-bottom: 25px;
    }
  }
  
  .ant-menu-item-group-title {
    padding-left: 0;
    color: ${props => props.theme.textColorDark};
    font-weight: bold;
    text-transform: uppercase;
    font-size: ${props => props.theme.textFontSizeSmall};
  }
  
  .ant-menu-item {
    padding-left: 0 !important;
    margin: 0 !important;
  }
  
  .ant-menu-item:hover,
  .ant-menu-item-selected {
    color: ${props => props.theme.secondaryBlue} !important;
    background-color: transparent !important;
  } 
`;

interface ParentProps {
  id?: string;
  mode?: MenuMode;
  selectable?: boolean;
  selectedKeys?: string[];
  defaultSelectedKeys?: string[];
  openKeys?: string[];
  defaultOpenKeys?: string[];
  onOpenChange?: (openKeys: string[]) => void;
  onSelect?: (param: SelectParam) => void;
  onDeselect?: (param: SelectParam) => void;
  onClick?: (param: ClickParam) => void;
  style?: React.CSSProperties;
  openAnimation?: string | object;
  openTransitionName?: string | object;
  className?: string;
  prefixCls?: string;
  multiple?: boolean;
  inlineIndent?: number;
  inlineCollapsed?: boolean;
  subMenuCloseDelay?: number;
  subMenuOpenDelay?: number;
  getPopupContainer?: (triggerNode: Element) => HTMLElement;
  focusable?: boolean;
}

export const Menu: React.SFC<ParentProps> = (props) => (
  <StyledMenu {...props}>
    {props.children}
  </StyledMenu>
);
