import * as React from 'react';
import { compose, withProps } from 'recompose';
import { Card } from '../../../../shared/components/Card';
import { FlexContainerProps, FlexContainer } from '../../../../shared/styled/shared/containers';
import { MemberAvatar } from '../../../../shared/components/user/MemberAvatar';
import { NomineePersonalStatement } from './NomineePersonalStatement';
import { NomineeCardAction } from './NomineeCardAction';
import { MemberLink } from '../../../../shared/components/user/MemberLink';
import { ElectionNominee, User } from '@narrative/shared';
import styled from '../../../../shared/styled';

const AvatarWrapper = styled<FlexContainerProps>(FlexContainer)`
  position: absolute;
  left: 0;
  right: 0;
  top: -15px;
`;

const UserDetailsWrapper = styled<FlexContainerProps>(FlexContainer)`
  margin: 15px 0;
  
  a:first-child {
    line-height: 23px;
  }
`;

const CardActionWrapper = styled<FlexContainerProps>(FlexContainer)`
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 50px;
  border-top: 1px solid #e9e9e9;
`;

interface WithProps {
  user: User;
  personalStatement?: string;
}

interface ParentProps {
  nominee: ElectionNominee;
  isCurrentUser?: boolean;
  electionOid: string;
}

type Props =
  ParentProps &
  WithProps;

export const NomineeCardComponent: React.SFC<Props> = (props) => {
  const { user, personalStatement, isCurrentUser, electionOid } = props;

  return (
    <Card height={275} noBoxShadow={true} hoverable={true} bodyStyle={{padding: 18}}>
      <AvatarWrapper justifyContent="center">
        <MemberAvatar user={user}/>
      </AvatarWrapper>

      <UserDetailsWrapper centerAll={true} column={true}>
        <div>
          <MemberLink user={user} size="large" weight={400} color="dark" appendUsername={true} />
        </div>
      </UserDetailsWrapper>

      <FlexContainer justifyContent="center">
        <NomineePersonalStatement
          personalStatement={personalStatement}
          isCurrentUser={isCurrentUser}
          electionOid={electionOid}
        />
      </FlexContainer>

      <CardActionWrapper centerAll={true}>
        <NomineeCardAction electionOid={electionOid} isCurrentUser={isCurrentUser}/>
      </CardActionWrapper>
    </Card>
  );
};

export const NomineeCard = compose(
  withProps((props: ParentProps) => {
    const { nominee } = props;

    const user =
      nominee &&
      nominee.nominee;
    const personalStatement =
      nominee &&
      nominee.personalStatement;

    return { user, personalStatement };
  })
)(NomineeCardComponent) as React.ComponentClass<ParentProps>;
