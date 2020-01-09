import * as React from 'react';
import { HeaderAvatarMenu } from './HeaderAvatarMenu';
import { MemberAvatar } from '../../../shared/components/user/MemberAvatar';
import { User } from '@narrative/shared';
import styled from '../../../shared/styled';

const AvatarWrapper = styled.div`
  cursor: pointer;
`;

interface ParentProps {
  currentUser: User;
}

export const HeaderAvatar: React.SFC<ParentProps> = (props) => {
  const { currentUser } = props;

  return (
    <AvatarWrapper>
      <HeaderAvatarMenu currentUser={currentUser}>
        <MemberAvatar size={35} user={currentUser} link={false} />
      </HeaderAvatarMenu>
    </AvatarWrapper>
  );
};
