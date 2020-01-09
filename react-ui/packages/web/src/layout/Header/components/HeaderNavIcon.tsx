import * as React from 'react';
import { Icon } from 'antd';
import { NavLink } from '../../../shared/components/NavLink';
import { generatePath } from 'react-router';
import { WebRoute } from '../../../shared/constants/routes';
import { Link, LinkSecurerFunction } from '../../../shared/components/Link';

interface ParentProps {
  icon: string;
  to?: WebRoute;
  href?: string;
  linkSecurer?: LinkSecurerFunction;
}

export const HeaderNavIcon: React.SFC<ParentProps> = (props) => {
  const { icon, to, href, linkSecurer} = props;

  const navIcon = <Icon type={icon} style={{ fontSize: 20 }}/>;

  // jw: if we were given an href then we need to create a more standard link that targets blank.
  if (href) {
    return (
      <Link.Anchor href={href} linkSecurer={linkSecurer} color="light" target="_blank">
        {navIcon}
      </Link.Anchor>
    );
  }

  if (!to) {
    // todo:error-handling: We should report this unexpected condition to the server.
    return null;
  }

  if (linkSecurer) {
    return (
      <Link to={to} linkSecurer={linkSecurer} color="light">
        {navIcon}
      </Link>
    );
  }

  return (
    <NavLink to={generatePath(to)}>
      {navIcon}
    </NavLink>
  );
};
