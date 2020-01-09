import * as React from 'react';
import { MemberAvatar, MemberAvatarProps } from './MemberAvatar';
import { FlexContainer, FlexContainerProps } from '../../styled/shared/containers';
import { Tooltip } from 'antd';
import { Omit } from 'recompose';
import { User } from '@narrative/shared';
import styled, { css } from '../../styled/index';

const size = {
  sm: css`
    width: 20px !important;
    height: 20px !important;
    line-height: 20px !important;
  `,
  default: css`
    width: 24px !important;
    height: 24px !important;
    line-height: 24px !important;
  `,
  lg: css`
    width: 28px !important;
    height: 28px !important;
    line-height: 28px !important;
  `
};

const AvatarListWrapper = styled<FlexContainerProps>(FlexContainer)`
  position: relative;
  left: 5px;
`;

const StyledMemberAvatar = styled<
  MemberAvatarProps & { index: number; listSize?: AvatarListSize; }
  >(({ index, listSize, ...rest }) => <MemberAvatar {...rest}/>)`
    ${props => props.listSize ? size[props.listSize] : size.default};
    position: relative;
    border: 1.5px solid #fff;
    right: ${props => 5 * props.index}px;
    z-index: ${props => 100 - props.index};
  `;

type AvatarListSize = 'sm' | 'default' | 'lg';

export interface MemberAvatarListProps extends Omit<MemberAvatarProps, 'user'> {
  users: User[];
  listSize?: AvatarListSize;
}

export const MemberAvatarList: React.SFC<MemberAvatarListProps> = (props) => {
  const { users, listSize } = props;

  return (
    <AvatarListWrapper>
      {users.map((user, i) => (
        <Tooltip key={i} placement="top" title={user.username}>
          <StyledMemberAvatar user={user} listSize={listSize} index={i + 1}/>
        </Tooltip>
      ))}
    </AvatarListWrapper>
  );
};
