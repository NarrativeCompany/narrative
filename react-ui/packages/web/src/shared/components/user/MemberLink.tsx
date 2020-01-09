import * as React from 'react';
import { Link, LinkStyleProps } from '../Link';
import { generatePath } from 'react-router';
import { WebRoute } from '../../constants/routes';
import { User } from '@narrative/shared';
import { MemberReputationBadge } from './MemberReputationBadge';
import { MemberUsername } from './MemberUsername';

interface ParentProps {
  user: User;
  targetBlank?: boolean;
  hideBadge?: boolean;
  dontApplySizeToBadge?: boolean;
  appendUsername?: boolean;
  itemProp?: string;
}

type Props =
  ParentProps &
  LinkStyleProps;

export const MemberLink: React.SFC<Props> = (props) => {
  const {user, targetBlank, hideBadge, dontApplySizeToBadge, appendUsername, ...memberLinkProps} = props;
  const { username } = user;

  if (user.deleted || username === '') {
    return <React.Fragment>{props.children ? props.children : user.displayName}</React.Fragment>;
  }

  return (
    <React.Fragment>
      <Link
        {...memberLinkProps}
        target={targetBlank ? '_blank' : undefined}
        to={generatePath(WebRoute.UserProfile, { username })}
      >
        {props.children ? props.children : user.displayName}
      </Link>

      {!hideBadge &&
        <MemberReputationBadge
          user={user}
          badgeSize={dontApplySizeToBadge ? undefined :  memberLinkProps.size}
          noMarginRight={appendUsername}
        />
      }

      {/* jw: we need to force a space here so that the browser knows it can wrap the username independently */}
      {appendUsername &&
        <React.Fragment>
          {' '}
          <MemberUsername user={user} targetBlank={targetBlank}/>
        </React.Fragment>
      }
    </React.Fragment>
  );
};
