import { defineMessages } from 'react-intl';

export const ElectionStateMessageMessages = defineMessages({
  BeforeElection: {
    id: 'electionStageMessage.beforeElection',
    defaultMessage: 'Moderator nominees are now being accepted for the Niche ‘{nicheName}.’'
  },
  DuringElection: {
    id: 'electionStageMessage.duringElection',
    defaultMessage: 'Moderator Election for the Niche ’{nicheName}’ is now live!'
  },
  AfterElection: {
    id: 'electionStageMessage.afterElection',
    defaultMessage: 'The election has ended.'
  }
});
