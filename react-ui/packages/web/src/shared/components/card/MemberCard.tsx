import { CurrentUserFollowedItem, User } from '@narrative/shared';
import * as React from 'react';
import { FlexContainer } from '../../styled/shared/containers';
import { MemberAvatar } from '../user/MemberAvatar';
import { MemberLink } from '../user/MemberLink';
import { CardButtonFooter, CardProps, StyledCard } from '../Card';
import { compose, } from 'recompose';
import { withExtractedCurrentUser } from '../../containers/withExtractedCurrentUser';
import {
  FollowMemberButton,
  withIncludeFollowMemberButton,
  WithIncludeFollowMemberButtonProps
} from '../user/FollowMemberButton';

interface ParentProps {
  user: User;
  // jw: in some cases we will need to get this from the outside.
  // jw: note: because some of these are coming from Apollo, we need to support null. Converting to undefined below
  //     since the follow member button should remain tight for now.
  followedUser?: CurrentUserFollowedItem | null;
  // jw: since we are including HOCs here, we can not extend these props and then spread them on it. We will be adding
  //     a lot of unrecognized properties to the antd.Card. Thus, we need to sandbox these together.
  cardProps?: CardProps;
}

export type MemberCardProps =
  ParentProps &
  WithIncludeFollowMemberButtonProps;

const MemberCardComponent: React.SFC<MemberCardProps> = (props) => {
  const { user, followedUser, includeFollowButton, cardProps } = props;

  return (
    <StyledCard {...cardProps}>
      <FlexContainer column={true} centerAll={true}>
        <MemberAvatar user={user} size={115} style={{ marginBottom: 10 }}/>
        <MemberLink user={user} size="large" color="dark" appendUsername={true} />
      </FlexContainer>

      {includeFollowButton &&
        <CardButtonFooter>
          <FollowMemberButton user={user} followedUser={followedUser || undefined} />
        </CardButtonFooter>
      }
    </StyledCard>
  );
};

export const MemberCard = compose(
  withExtractedCurrentUser,
  withIncludeFollowMemberButton
)(MemberCardComponent) as React.ComponentClass<ParentProps>;
