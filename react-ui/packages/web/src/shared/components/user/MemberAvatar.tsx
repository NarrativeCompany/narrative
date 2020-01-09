import * as React from 'react';
import { compose, lifecycle, Omit } from 'recompose';
import { AvatarProps } from 'antd/lib/avatar';
import { User, withState, WithStateProps } from '@narrative/shared';
import { Avatar } from 'antd';
import defaultAvatar from '../../../assets/default-avatar@1x.png';
import { MemberLink } from './MemberLink';

export interface MemberAvatarProps extends Omit<AvatarProps, 'src'> {
  user: User;
  link?: boolean;
}

interface State {
  avatarUrl: string | undefined;
}

type Props =
  MemberAvatarProps &
  WithStateProps<State>;

const MemberAvatarComponent: React.SFC<Props> = (props) => {
  const { setState, state, user, link, ...avatarProps } = props;

  const avatar = (
    <Avatar
      {...avatarProps}
      src={state.avatarUrl}
      alt={props.alt || user.displayName}
      onError={() => {
        setState((ss) => ({ ...ss, avatarUrl: defaultAvatar }));
        return false;
      }}
    />
  );

  // jw: let's default to linking the avatar. There are few times we do not want to link, so defaulting to true is best.
  if (link === undefined || link) {
    return (
      <MemberLink user={user} hideBadge={true}>
        {avatar}
      </MemberLink>
    );
  }

  return avatar;
};

export const MemberAvatar = compose(
  withState<State>({ avatarUrl: defaultAvatar }),
  lifecycle<Props, {}>({
    componentDidMount() {
      const { user, setState } = this.props;

      if (user && user.avatarSquareUrl) {
        setState((ss) => ({ ...ss, avatarUrl: user.avatarSquareUrl! }));
      }
    }
  })
)(MemberAvatarComponent) as React.ComponentClass<MemberAvatarProps>;
