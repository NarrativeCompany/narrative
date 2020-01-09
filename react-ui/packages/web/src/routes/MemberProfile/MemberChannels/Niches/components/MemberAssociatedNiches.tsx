import * as React from 'react';
import { FormattedMessage } from 'react-intl';
import { SectionHeader } from '../../../../../shared/components/SectionHeader';
import { MemberAssociatedNiche } from './MemberAssociatedNiche';
import { NicheUserAssociation, User } from '@narrative/shared';
import styled from '../../../../../shared/styled';

interface ParentProps {
  associations: NicheUserAssociation[];
  user: User;
  isCurrentUser: boolean;
  titleMessage: FormattedMessage.MessageDescriptor;
  titleMessageForCurrentUser: FormattedMessage.MessageDescriptor;
}

const SectionContainer = styled.div`
  margin-bottom: 30px;
`;

export const MemberAssociatedNiches: React.SFC<ParentProps> = (props) => {
  const { associations, user, isCurrentUser, titleMessage, titleMessageForCurrentUser } = props;

  if (!associations.length) {
    return null;
  }

  const title = isCurrentUser ?
    <FormattedMessage {...titleMessageForCurrentUser}/> :
    <FormattedMessage {...titleMessage} values={{displayName: user.displayName}}/>;

  return (
    <SectionContainer>
      <SectionHeader title={title}/>

      {associations.map((association: NicheUserAssociation) =>
        <MemberAssociatedNiche key={association.niche.oid} association={association}/>
      )}
    </SectionContainer>
  );
};
