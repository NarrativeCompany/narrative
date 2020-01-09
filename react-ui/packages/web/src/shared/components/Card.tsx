import * as React from 'react';
import { Card as AntCard } from 'antd';
import { CardProps as AntCardProps } from 'antd/lib/card';
import { ThemeColorType } from '../styled/theme';
import styled from '../styled';
import { MemberCard } from './card/MemberCard';
import { ChannelCard } from './card/ChannelCard';

/**
 * Shared Components For Rendering On Cards
 */

export const CardButtonFooter = styled.div`
  margin-top: 32px;

  .ant-btn,
  .ant-btn:hover,
  .ant-btn:focus,
  .ant-btn:active,
  .ant-btn-loading {
    position: absolute !important;
    bottom: 0;
    right: 0;
    left: 0;
    width: 100%;
    border-radius: 0 0 6px 6px;
    border: none;
    border-top: 1px solid #e9e9e9;
    font-size: 12px;
    height: 35px;
    padding-left: 0 !important;
    padding-right: 0 !important;
  }
`;

/**
 * Type definitions for the base styled card component
 */

export type CardColor = ThemeColorType;

interface ParentProps {
  color?: CardColor;
  height?: number;
  bottomMargin?: number;
  noBoxShadow?: boolean;
}

export type CardProps =
  ParentProps &
  AntCardProps;

/**
 * End of base types
 */

/**
 * base styled card definition
 * The styled card is the card in which sub-component cards inherit types and basic styles
 * Overrides of type extension for sub-components should be grouped with their respective definition
 */

export const StyledCard = styled<CardProps>(
  ({height, color, noBoxShadow, bottomMargin, ...rest}) => <AntCard {...rest}/>)`
  &.ant-card {
    height: ${p => `${p.height}px` || 'auto'}
    cursor: auto;
    border-radius: 6px;
    
    ${p => p.color && `
      background: ${p.theme[p.color]};
      border: ${p.theme[p.color]};
    `}
    
    ${props => !props.noBoxShadow && `
       box-shadow: 0 2px 12px rgba(0, 0, 0, 0.09);
       border: none;
    `}
    
    ${p => p.bottomMargin && `
      margin-bottom: ${p.bottomMargin}px;
    `}
  }
  
  &.ant-card-contain-tabs {
    .ant-card-head {
      padding: 0;
    }
    
    .ant-tabs-tab {
      font-size: ${p => p.theme.textFontSizeDefault};
      padding: 8px 0 !important;
      margin: 0 !important;
    }
    
    .ant-tabs-ink-bar {
      background-color: ${p => p.theme.primaryBlue};
    }
  }
`;

/**
 * End of base card definition
 */

export class Card extends React.Component<CardProps, {}> {
  static User = MemberCard;
  static Channel = ChannelCard;

  render () {
    return <StyledCard {...this.props}/>;
  }
}
