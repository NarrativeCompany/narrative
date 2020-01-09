import * as React from 'react';
import { User } from '@narrative/shared';
import { LinkStyleProps } from '../Link';
import { MemberLink } from './MemberLink';

interface ParentProps {
  user: User;
  targetBlank?: boolean;
}

type Props =
  ParentProps &
  LinkStyleProps;

export const MemberUsername: React.SFC<Props> = (props) => {
  const { user, color } = props;

  if (user.deleted || user.username === '') {
    return <React.Fragment>{user.displayName}</React.Fragment>;
  }

  return (
    <MemberLink className="member-username" {...props} hideBadge={true} color={color || 'light'}>
      @{user.username}
    </MemberLink>
  );
};
