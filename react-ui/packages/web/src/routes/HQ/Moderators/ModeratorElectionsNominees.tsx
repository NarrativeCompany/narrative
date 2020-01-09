import * as React from 'react';
import { compose, withProps } from 'recompose';
import { Paragraph } from '../../../shared/components/Paragraph';
import { MemberAvatarList } from '../../../shared/components/user/MemberAvatarList';
import { FlexContainer, FlexContainerProps } from '../../../shared/styled/shared/containers';
import { FormattedMessage } from 'react-intl';
import { User } from '@narrative/shared';
import { ModeratorElectionsMessages } from '../../../shared/i18n/ModeratorElectionsMessages';
import styled from '../../../shared/styled';

const NomineeCountWrapper = styled.div`
  margin-right: 10px;
`;

const AvatarListWrapper = styled<FlexContainerProps>(FlexContainer)`
  p {
    align-self: flex-end;
  }
`;

interface WithProps {
  totalNominees: number;
  memberAvatarList: User[] | null;
  totalOtherMembers: null;
}

interface ParentProps {
  nominees: User[];
}

type Props =
  ParentProps &
  WithProps;

export const ModeratorElectionsNomineesComponent: React.SFC<Props> = (props) => {
  const { totalNominees, memberAvatarList, totalOtherMembers } = props;

  return (
    <FlexContainer alignItems="center">
      <NomineeCountWrapper>
        <Paragraph>{totalNominees}</Paragraph>
      </NomineeCountWrapper>

      {memberAvatarList &&
      <AvatarListWrapper>
        <MemberAvatarList users={memberAvatarList} listSize="sm"/>

        {totalOtherMembers &&
        <React.Fragment>
          <Paragraph size="small" color="light">
            <FormattedMessage {...ModeratorElectionsMessages.ModeratorNomineesAnd}/>
          </Paragraph>

          <Paragraph size="small" color="dark">
            &nbsp;{totalOtherMembers} <FormattedMessage {...ModeratorElectionsMessages.ModeratorNomineesOther}/>
          </Paragraph>
        </React.Fragment>}
      </AvatarListWrapper>}
    </FlexContainer>
  );
};

export const ModeratorElectionsNominees = compose(
  withProps((props: ParentProps) => {
    const { nominees } = props;

    const totalNominees = nominees.length;
    const memberAvatarList = totalNominees ? nominees.slice(0, 3) : null;
    const totalOtherMembers = totalNominees > 3 ? totalNominees - 3 : null;

    return { totalNominees, memberAvatarList, totalOtherMembers };
  })
)(ModeratorElectionsNomineesComponent) as React.ComponentClass<ParentProps>;
