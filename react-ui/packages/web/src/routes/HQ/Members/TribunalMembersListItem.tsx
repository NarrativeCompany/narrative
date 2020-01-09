import * as React from 'react';
import { User } from '@narrative/shared';
import { FlexContainer, FlexContainerProps } from '../../../shared/styled/shared/containers';
import { MemberAvatar } from '../../../shared/components/user/MemberAvatar';
import { MemberLink } from '../../../shared/components/user/MemberLink';
import styled from '../../../shared/styled';

interface Props {
  member?: User;
}

const TribunalMembersItemWrapper = styled<FlexContainerProps>(FlexContainer)`
  cursor: pointer;
  min-height: 250px;
  
  @media screen and (max-width: 767px) {
    align-items: center;
  }
`;

const TribunalMembersInnerWrapper = styled<FlexContainerProps>(FlexContainer)`
  max-width: fit-content;
`;

const EmptyTribunalMemberItem = styled.div`
  width: 155px;
  height: 155px;
  background: #F9FAFB;
  border: 1px solid ${props => props.theme.borderGrey};
  border-radius: 50%;
`;

export const TribunalMembersListItem: React.SFC<Props> = (props) => {
  const { member } = props;

  if (!member) {
    return (
      <TribunalMembersItemWrapper column={true}>
        <EmptyTribunalMemberItem/>
      </TribunalMembersItemWrapper>
    );
  }

  return (
    <TribunalMembersItemWrapper column={true}>
      <TribunalMembersInnerWrapper column={true} alignItems="center">
        <MemberAvatar size={175} user={member} style={{marginBottom: '15px'}}/>

        <MemberLink user={member} color="dark" size="large" weight={400}/>
      </TribunalMembersInnerWrapper>
    </TribunalMembersItemWrapper>
  );
};
