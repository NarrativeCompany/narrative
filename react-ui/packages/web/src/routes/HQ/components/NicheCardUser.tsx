import * as React from 'react';
import { Avatar as AntAvatar } from 'antd';
import { FlexContainer, FlexContainerProps } from '../../../shared/styled/shared/containers';
import { FormattedMessage } from 'react-intl';
import { NicheCardUserMessages } from '../../../shared/i18n/NicheCardUserMessages';
import { AvatarProps } from 'antd/lib/avatar';
import styled from '../../../shared/styled/index';
import defaultAvatar from '../../../assets/default-avatar@1x.png';
import { User } from '@narrative/shared';
import { MemberUsername } from '../../../shared/components/user/MemberUsername';
import { MemberReputationBadge } from '../../../shared/components/user/MemberReputationBadge';

const UserWrapper = styled<FlexContainerProps>(FlexContainer)`
  margin-bottom: 10px;
  font-size: ${props => props.theme.textFontSizeSmall};

  span {
    color: ${props => props.theme.textColorLight};
  }

  strong {
    color: ${props => props.theme.textColorDark};
  }
`;

const AvatarWrapper = styled<FlexContainerProps>(FlexContainer)`
  position: absolute;
  top: -11px;
  left: 0;
  right: 0;
  width: 100%;
`;

const Avatar = styled<AvatarProps>((props) => <AntAvatar {...props}/>)`
  border: 2px solid #fff;
  background: #fff !important;
`;

interface PropsProps {
  user: User;
  targetBlank?: boolean;
  forAuctionPendingPayment?: boolean;
}

export const NicheCardUser: React.SFC<PropsProps> = (props) => {
  const { targetBlank, user, forAuctionPendingPayment, user: { avatarSquareUrl } } = props;

  return (
    <UserWrapper column={true}>
      <AvatarWrapper justifyContent="center">
        <Avatar src={avatarSquareUrl || defaultAvatar} size="large"/>
      </AvatarWrapper>

      <FlexContainer>
        <FormattedMessage
          {...(forAuctionPendingPayment ? NicheCardUserMessages.WonBy : NicheCardUserMessages.SuggestedBy)}
        />
        &nbsp;

        <MemberUsername
          user={user}
          color="dark"
          size="small"
          targetBlank={targetBlank}
        />
        <MemberReputationBadge user={user} badgeSize="small" />
      </FlexContainer>
    </UserWrapper>
  );
};
