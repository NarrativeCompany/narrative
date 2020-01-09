import * as React from 'react';
import { FlexContainer, FlexContainerProps } from '../../../shared/styled/shared/containers';
import { WebRoute } from '../../../shared/constants/routes';
import { NavLink } from '../../../shared/components/NavLink';
import { FormattedMessage } from 'react-intl';
import styled, { css } from '../../../shared/styled';
import { HeaderNavMessages } from '../../../shared/i18n/HeaderNavMessages';
import { compose } from 'recompose';
import {
  withExtractedCurrentUser,
  WithExtractedCurrentUserProps
} from '../../../shared/containers/withExtractedCurrentUser';
import { mediaQuery } from '../../../shared/styled/utils/mediaQuery';

function getMediumDownStyling(props: ParentProps) {
  // jw: for mobile let's fix the header as, ironically, a footer.
  if (props.isMobile) {
    return css`
      display: flex;
      box-shadow: 0 -2px 10px 0 rgba(0,0,0,0.05);
      transition: all .15s ease-in-out;
      position: fixed;
      z-index: 2000;
      bottom: 0;
      left: 0;
      right: 0;
      background: #fff;
      padding: 15px 10px;
      border-top: 1px solid ${p => p.theme.borderGrey};
      justify-content: space-between;
    `;
  }

  // jw: hide the standard header when at mobile viewports.
  return css`display: none;`;
}

export const HeaderNavWrapper = styled<FlexContainerProps & {isMobile?: boolean}>(FlexContainer)`
  transition: all .15s ease-in-out;

  // jw: let's hide the mobile menu by default, and allow the medium down CSS to show it.
  ${p => p.isMobile && css`
    display: none;
  `}

  // jw: the requirements for medium down changes depending on whether we are mobile, so let's use a function for that.
  ${mediaQuery.md_down`
    ${p => getMediumDownStyling(p)}
  `}
`;

interface ParentProps {
  isMobile?: boolean;
}

type Props =
  ParentProps &
  WithExtractedCurrentUserProps;

const HeaderNavComponent: React.SFC<Props> = (props) => {
  const { isMobile, currentUser } = props;

  const forGuest = !currentUser;

  return (
    <HeaderNavWrapper centerAll={true} isMobile={isMobile}>

      {/* jw: the / Home route is branded differently depending on whether you are logged in or not. */}
      <NavLink color="bold" isHeaderLink={true} exact={true} to={WebRoute.Home}>
        <FormattedMessage {...(forGuest ? HeaderNavMessages.DiscoverLink : HeaderNavMessages.HomeLink)}/>
      </NavLink>

      {/* jw: since / Home is the home page for users, we need to expose the disover page separately. */}
      {!forGuest &&
        <NavLink color="bold" isHeaderLink={true} exact={true} to={WebRoute.Discover}>
          <FormattedMessage {...HeaderNavMessages.DiscoverLink}/>
        </NavLink>
      }

      <NavLink
        color="bold"
        isHeaderLink={true}
        to={WebRoute.HQ}
        isActive={(_, location) => location && location.pathname.includes(WebRoute.HQ)}
      >
        <FormattedMessage {...HeaderNavMessages.HQLink}/>
      </NavLink>
    </HeaderNavWrapper>
  );
};

export const HeaderNav = compose(
  withExtractedCurrentUser,
)(HeaderNavComponent) as React.ComponentClass<ParentProps>;
