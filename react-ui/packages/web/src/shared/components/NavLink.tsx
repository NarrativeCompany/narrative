import * as React from 'react';
import { NavLink as RouterNavLink, NavLinkProps as RouterNavLinkProps } from 'react-router-dom';
import styled, { theme } from '../styled';

const StyledNavLink = styled<NavLinkProps>(({isHeaderLink, color, ...rest}) => <RouterNavLink {...rest}/>)`
  transition: all .15s ease-in-out;
  text-decoration: none !important;
  ${props => props.isHeaderLink && `
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 8px 20px;
    border-radius: 4px;
    
    &:hover
    &.active {
      transition: all .15s ease-in-out;
    }
  `}
  
  span {
    color: ${props => getNavLinkColor(props.color)};
    transition: all .15s ease-in-out;
  }
  
  &:hover {
    span {
      color: ${props => props.theme.primaryBlue};
      transition: all .15s ease-in-out;
    }
  }
  
  &.active {
    span {
      color: ${props => props.theme.secondaryBlue};
      transition: all .15s ease-in-out;
    }
  }
`;

function getNavLinkColor (color?: NavLinkColor) {
  if (!color || color === 'default') {
    return theme.textColorLight;
  }

  return theme.textColorDark;
}

type NavLinkColor = 'default' | 'bold';

interface ParentProps {
  color?: NavLinkColor;
  isHeaderLink?: boolean;
}

export type NavLinkProps =
  ParentProps &
  RouterNavLinkProps;

export const NavLink: React.SFC<NavLinkProps> = (props) => {
  const { children, ...navLinkProps } = props;

  return (
    <StyledNavLink {...navLinkProps}>
      <span>
        {children}
      </span>
    </StyledNavLink>
  );
};
