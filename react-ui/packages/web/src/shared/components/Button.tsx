import * as React from 'react';
import { Button as AntButton } from 'antd';
import {
  ButtonHTMLType,
  ButtonSize,
  ButtonType as AntButtonType,
  ButtonShape
} from 'antd/lib/button/button';
import styled, { css } from '../styled';

const buttonSize = {
  small: css`
    height: ${props => props.theme.smallButtonHeight};
    /* bl: disabling this line-height, as it throws off vertical centering of text in these small buttons. */
    //line-height: ${props => props.theme.smallButtonHeight};
    font-size: ${props => props.theme.smallButtonFontSize};
    padding: ${props => props.theme.smallButtonPadding};
  `,
  default: css`
    height: ${props => props.theme.defaultButtonHeight};
    line-height: ${props => props.theme.defaultButtonHeight};
    font-size: ${props => props.theme.defaultButtonFontSize};
    padding: ${props => props.theme.defaultButtonPadding};
  `,
  large: css`
    height: ${props => props.theme.largeButtonHeight};
    line-height: ${props => props.theme.largeButtonHeight};
    font-size: ${props => props.theme.largeButtonFontSize};
    padding: ${props => props.theme.largeButtonPadding};
  `
};

const buttonType = {
  default: css`
    background-color: ${props => props.theme.primaryBlack};
    border-color: ${props => props.theme.primaryBlack};
    color: #fff;
    
    &:hover,
    &:active,
    &:focus {
      background-color: ${props => props.theme.secondaryBlack};
      border-color: ${props => props.theme.secondaryBlack};
      color: #fff;
    }
  `,
  primary: css`
    background-color: ${props => props.theme.primaryBlue};
    border-color: ${props => props.theme.primaryBlue};
    color: #fff;
    
    &:hover,
    &:active,
    &:focus {
      background-color: ${props => props.theme.secondaryBlue};
      border-color: ${props => props.theme.secondaryBlue};
      color: #fff;
    }
  `,
  ghost: css`
    background-color: tranparent;
    border-color: ${props => props.theme.secondaryBlue};
    color: ${props => props.theme.secondaryBlue};
    
    &:hover,
    &:active,
    &:focus {
      background-color: transparent;
      border-color: ${props => props.theme.primaryBlue};
      color: ${props => props.theme.primaryBlue};
    }
  `,
  'ghost-grey': css`
    background-color: transparent;
    border-color: ${props => props.theme.textColor};
    color: ${props => props.theme.textColor};
    
    &:hover,
    &:active,
    &:focus {
      background-color: transparent;
      border-color: ${props => props.theme.textColor};
      color: ${props => props.theme.textColor};
    }
  `,
  danger: css`
    background-color: ${props => props.theme.primaryRed};
    border-color: ${props => props.theme.primaryRed};
    color: #fff;
    
    &:hover,
    &:active,
    &:focus {
      background-color: ${props => props.theme.secondaryRed};
      border-color: ${props => props.theme.secondaryRed};
      color: #fff;
    }
  `,
  pink: css`
    background-color: ${props => props.theme.primaryPink};
    border-color: ${props => props.theme.primaryPink};
    color: #fff;
    
    &:hover,
    &:active,
    &:focus {
      background-color: ${props => props.theme.primaryPink};
      border-color: ${props => props.theme.primaryPink};
      color: #fff;
    }
  `,
  'ghost-orange': css`
    background-color: #fff;
    border-color: transparent;
    color: ${props => props.theme.primaryOrange};
    
    &:hover,
    &:active,
    &:focus {
      background-color: #fff;
      border-color: transparent;
      color: ${props => props.theme.secondaryOrange};
    }
  `,
  green: css`
    background-color: ${props => props.theme.brightGreen};
    border-color: transparent;
    color: #fff;
    
    &:hover,
    &:active,
    &:focus {
      background-color: #fff;
      border-color: ${props => props.theme.brightGreen};
      color: ${props => props.theme.brightGreen};
    }
  `
};

export type ButtonType = AntButtonType | 'pink' | 'ghost-orange' | 'ghost-grey' | 'green';
interface BaseButtonProps {
  type?: ButtonType;
  icon?: string;
  shape?: ButtonShape;
  size?: ButtonSize;
  loading?: boolean | {
    delay?: number;
  };
  prefixCls?: string;
  className?: string;
  ghost?: boolean;
  block?: boolean;
}

interface ParentProps {
  onClick?: () => void;
  style?: React.CSSProperties;
  htmlType?: ButtonHTMLType;
  disabled?: boolean;
  width?: number;
  href?: string;
  target?: string;
  noShadow?: boolean;
}

export type ButtonProps =
  ParentProps &
  BaseButtonProps;

export const Button = styled<ButtonProps>(({width, type, noShadow, ...rest}) => <AntButton {...rest}/>)`
  &.ant-btn {
    // global ant button style overrides
    text-transform: uppercase;
    border-radius: ${props => props.theme.buttonBorderRadius};
    
    // overrides based on passed props
    ${p => !p.noShadow && 'box-shadow: 0 2px 12px rgba(0, 0, 0, 0.075);'};
    ${p => p.width && `min-width: ${p.width}px;`}
    ${p => p.size ? buttonSize[p.size] : buttonSize.default};
    ${p => p.type ? buttonType[p.type] : buttonSize.default};
  }
`;
