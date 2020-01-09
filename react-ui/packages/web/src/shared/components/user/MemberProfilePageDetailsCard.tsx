import * as React from 'react';
import { UserDetail } from '@narrative/shared';
import { Card, CardButtonFooter } from '../Card';
import styled, { theme } from '../../styled';
import { Tag } from 'antd';
import { MemberLink } from './MemberLink';
import { LocalizedTime } from '../LocalizedTime';
import * as moment from 'moment-timezone';
import { MemberProfileWrapperMessages } from '../../i18n/MemberProfileWrapperMessages';
import { FormattedMessage } from 'react-intl';
import { Avatar } from './Avatar';
import { compose, withProps } from 'recompose';
import { Paragraph } from '../Paragraph';
import { FlexContainer, FlexContainerProps } from '../../styled/shared/containers';
import { User } from '@narrative/shared';
import {
  FollowMemberButton,
  withIncludeFollowMemberButton,
  WithIncludeFollowMemberButtonProps
} from './FollowMemberButton';
import { Button } from '../Button';
import { WebRoute } from '../../constants/routes';

export const ProfileDetailsCard = styled(Card)`
  &.ant-card {
    margin-bottom: 20px;
  }
`;

const DisplayNameWrapper = styled<FlexContainerProps>(FlexContainer)`
  margin-bottom: 25px;
`;

const TagsWrapper = styled.div`
  .ant-tag {
    &:last-child {
      margin-right: 0;
    }
  }
`;

const ManageAccountButton = styled(Button)`
  &.ant-btn {
    text-transform: none;
  }
`;

interface WithProps extends WithIncludeFollowMemberButtonProps {
  user: User;
  userLabels: string[];
  joined: string;
  lastVisit: string;
}

interface ParentProps {
  userDetail: UserDetail;
  isCurrentUser: boolean;
}

type Props = ParentProps & WithProps;

export const MemberProfilePageDetailsCardComponent: React.SFC<Props> = (props) => {
  const { user, userLabels, joined, lastVisit, includeFollowButton, isCurrentUser } = props;

  let footerButton: React.ReactNode | undefined;
  if (isCurrentUser) {
    footerButton = (
      <ManageAccountButton href={WebRoute.MemberCP} type="primary">
        <FormattedMessage {...MemberProfileWrapperMessages.ManageAccount}/>
      </ManageAccountButton>
    );

  } else if (includeFollowButton) {
    footerButton = <FollowMemberButton user={user} />;
  }

  return (
    <ProfileDetailsCard>
      <Avatar user={user} size={115}/>

      <FlexContainer centerAll={true} column={true}>
        <DisplayNameWrapper centerAll={true} column={true}>
          <div>
            <MemberLink user={user} size="large" color="dark" weight={600}>
              {user.displayName}
            </MemberLink>
          </div>

          <Paragraph>
            @{user.username}
          </Paragraph>
        </DisplayNameWrapper>

        {userLabels.length > 0 &&
        <TagsWrapper>
          {userLabels.map((label: string, i) => (
            <Tag key={i} color={theme.secondaryBlue} style={{marginBottom: '5px', display: 'inline-block'}}>
              {label}
            </Tag>
          ))}
        </TagsWrapper>}

        {joined &&
        <Paragraph>
          <FormattedMessage {...MemberProfileWrapperMessages.Joined}/>&nbsp;
          <LocalizedTime dateOnly={true} time={joined}/>
        </Paragraph>}

        {lastVisit &&
        <Paragraph>
          <FormattedMessage {...MemberProfileWrapperMessages.LastVisit}/>:&nbsp;
          {moment(lastVisit != null ? lastVisit : '').fromNow()}
        </Paragraph>}
      </FlexContainer>

      {footerButton &&
        <CardButtonFooter>
          {footerButton}
        </CardButtonFooter>
      }
    </ProfileDetailsCard>
  );
};

export const MemberProfilePageDetailsCard = compose(
  withProps((props: ParentProps) => {
    const { userDetail } = props;

    const user = userDetail.user;
    const userLabels = user.labels || [];
    const joined = userDetail.joined;
    const lastVisit = userDetail.lastVisit;

    return { user, userLabels, joined, lastVisit };
  }),
  withIncludeFollowMemberButton
)(MemberProfilePageDetailsCardComponent) as React.ComponentClass<ParentProps>;
