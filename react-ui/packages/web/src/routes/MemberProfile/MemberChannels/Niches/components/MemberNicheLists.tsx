import * as React from 'react';
import { MemberChannelsMessages } from '../../../../../shared/i18n/MemberChannelsMessages';
import {
  User,
  NicheUserAssociation,
  NicheAssociationType
} from '@narrative/shared';
import { MemberAssociatedNiches } from './MemberAssociatedNiches';
import { NoResultsMessage } from '../../../../../shared/components/NoResultsMessage';

interface ParentProps {
  associations: NicheUserAssociation[];
  user: User;
  isCurrentUser: boolean;
}

export const MemberNicheLists: React.SFC<ParentProps> = (props) => {
  const { associations, user, isCurrentUser } = props;

  if (!associations.length) {
    if (isCurrentUser) {
      return null;
    }

    const { displayName } = user;

    return <NoResultsMessage message={MemberChannelsMessages.NoNiches} values={{displayName}}/>;
  }

  const ownedAssociations: NicheUserAssociation[] = [];
  const biddingAssociations: NicheUserAssociation[] = [];
  associations.forEach((association: NicheUserAssociation) => {
    if (association.type === NicheAssociationType.OWNER) {
      ownedAssociations.push(association);

    } else if (association.type === NicheAssociationType.BIDDER) {
      biddingAssociations.push(association);

      // todo:error-handling: we need to figure out how we want to report when we encounter an unexpected type.
    }
  });

  return (
    <React.Fragment>
      <MemberAssociatedNiches
        associations={ownedAssociations}
        user={user}
        isCurrentUser={isCurrentUser}
        titleMessage={MemberChannelsMessages.NichesUserOwns}
        titleMessageForCurrentUser={MemberChannelsMessages.NichesYouOwn}
      />
      <MemberAssociatedNiches
        associations={biddingAssociations}
        user={user}
        isCurrentUser={isCurrentUser}
        titleMessage={MemberChannelsMessages.NichesUserIsBiddingOn}
        titleMessageForCurrentUser={MemberChannelsMessages.NichesYoureBiddingOn}
      />
    </React.Fragment>
  );
};
