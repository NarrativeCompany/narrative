import * as React from 'react';
import { FlexContainer } from '../../../../shared/styled/shared/containers';
import { ModeratorElectionStat } from './ModeratorElectionStat';
import { Election } from '@narrative/shared';
import { ModeratorElectionDetailsMessages } from '../../../../shared/i18n/ModeratorElectionDetailsMessages';

interface ParentProps {
  election: Election;
}

export const ModeratorElectionStatsSection: React.SFC<ParentProps> = (props) => {
  const { election } = props;

  return (
    <FlexContainer style={{padding: '20px 0'}} centerAll={true} flexWrap="wrap">
      <ModeratorElectionStat
        label={ModeratorElectionDetailsMessages.AvailableSlotsLabel}
        value={election.availableSlots}
      />

      <ModeratorElectionStat
        label={ModeratorElectionDetailsMessages.TotalNomineesLabel}
        value={election.nomineeCount}
      />
    </FlexContainer>
  );
};
