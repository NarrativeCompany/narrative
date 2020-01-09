import * as React from 'react';
import styled from '../styled';

export const headingLineHeightBuffer = 4;

const H1 = styled<HeadingProps, 'h1'>('h1')`
  font-size: ${props => props.theme.h1FontSize}px;
  line-height: ${props => props.theme.h1FontSize + headingLineHeightBuffer}px;
`;

const H2 = styled<HeadingProps, 'h2'>('h2')`
  font-size: ${props => props.theme.h2FontSize}px;
  line-height: ${props => props.theme.h2FontSize + headingLineHeightBuffer}px;
`;

const H3 = styled<HeadingProps, 'h3'>('h3')`
  font-size: ${props => props.theme.h3FontSize}px;
  line-height: ${props => props.theme.h3FontSize + headingLineHeightBuffer}px;
`;

const H4 = styled<HeadingProps, 'h4'>('h4')`
  font-size: ${props => props.theme.h4FontSize}px;
  line-height: ${props => props.theme.h4FontSize + headingLineHeightBuffer}px;
`;

const H5 = styled<HeadingProps, 'h5'>('h5')`
  font-size: ${props => props.theme.h5FontSize}px;
  line-height: ${props => props.theme.h5FontSize + headingLineHeightBuffer}px;
`;

const H6 = styled<HeadingProps, 'h6'>('h6')`
  font-size: ${props => props.theme.h6FontSize}px;
  line-height: ${props => props.theme.h6FontSize + headingLineHeightBuffer}px;
`;

export type HeadingSize = 1 | 2 | 3 | 4 | 5 | 6;

export interface HeadingProps {
  size: HeadingSize;
  textAlign?: 'left' | 'center' | 'right';
  weight?: number | string;
  color?: string;
  noMargin?: boolean;
  uppercase?: boolean;
  isLink?: boolean;
  onClick?: () => void;
  style?: React.CSSProperties;
  id?: string;
  className?: string;
  itemProp?: string;
}

const HeadingComponent: React.SFC<HeadingProps> = (props) => {
  const { size } = props;
  const { textAlign, weight, color, noMargin, uppercase, isLink, ...rest } = props;

  switch (size) {
    case 1:
      return <H1 {...rest}/>;
    case 2:
      return <H2 {...rest}/>;
    case 3:
      return <H3 {...rest}/>;
    case 4:
      return <H4 {...rest}/>;
    case 5:
      return <H5 {...rest}/>;
    case 6:
      return <H6 {...rest}/>;
    default:
      throw new Error('getHeadingTag: size property must be provided');
  }
};

export const Heading = styled<HeadingProps>(HeadingComponent)`
    color: ${props => props.color ? props.color : props.theme.textColorDark};
    text-align: ${props => props.textAlign || 'initial' };
    // jw: since we are using Google fonts as our defaults, let's use 700 which is the only weight above normal (400)
    font-weight: ${props => props.weight || 700};
    margin: ${props => props.noMargin && 0};
    text-transform: ${props => props.uppercase && 'uppercase'};
    transition: all .15s ease-in-out;
    
    &:hover {
      ${props => props.isLink &&
    `color: ${props.theme.primaryBlue}; 
      transition: all .15s ease-in-out';
      cursor: pointer;`
    }
  `;
