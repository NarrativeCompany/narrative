import { defineMessages } from 'react-intl';

export const TribunalAppealCardMessages = defineMessages({
  AppealDetailsCommentCount: {
    id: 'tribunalAppealCard.appealDetailsCommentCount',
    defaultMessage: 'Appeal Details ({count, number} {count, plural, one {Comment} other {Comments}})'
  },
  AppealSubmittedByUserOnDateText: {
    id: 'tribunalAppealCard.appealSubmittedByUserOnDateText',
    defaultMessage: 'Appeal submitted by {user} on {date}'
  },
  TribunalVoteEnded: {
    id: 'tribunalAppealCard.tribunalVoteEnded',
    defaultMessage: 'Voting has ended.'
  }
});
