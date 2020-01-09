import * as React from 'react';
import { NominateCurrentUserButton } from './NominateCurrentUserButton';
import { ElectionNominee } from '@narrative/shared';
import { NomineeCard } from './NomineeCard';

interface ParentProps {
  electionOid: string;
  currentUserNominee: ElectionNominee | null;
}

export const CurrentUserNominee: React.SFC<ParentProps> = (props) => {
  const { electionOid, currentUserNominee } = props;

  if (!currentUserNominee) {
    return <NominateCurrentUserButton electionOid={electionOid}/>;
  }

  return (
    <NomineeCard
      electionOid={electionOid}
      nominee={currentUserNominee}
      isCurrentUser={true}
    />
  );
};
