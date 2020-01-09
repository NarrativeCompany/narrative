import { defineMessages } from 'react-intl';

export const MemberReferralDetailsMessages = defineMessages({
  Title: {
    id: 'memberReferralDetails.title',
    defaultMessage: '{displayName} - Referral Program'
  },
  HelpGrowMessage: {
    id: 'memberReferralDetailsIntro.helpGrowMessage',
    defaultMessage: 'The Narrative Referral Program was a program during the Alpha used to recruit the early ' +
      'community of Narrators and earn {nrveLink} rewards.'
  },
  TopTenDescription: {
    id: 'memberReferralDetailsIntro.topTenDescription',
    defaultMessage: 'If you are still among the top 10 recruiters when we conclude all rounds of the Referral ' +
      'Program, you will get a share of the 5,500 {nrveLink} bonus! You can {viewTheRulesLink} for more details.'
  },
  ViewTheRules: {
    id: 'memberReferralDetailsIntro.viewTheRules',
    defaultMessage: 'view the full rules'
  },
  ReferralDetailsRank: {
    id: 'memberReferralDetailsStats.rank',
    defaultMessage: 'Rank'
  },
  ReferralDetailsRankValue: {
    id: 'memberReferralDetailsStats.rankValue',
    defaultMessage: '#{rank, number}'
  },
  ReferralDetailsUnranked: {
    id: 'memberReferralDetailsStats.unranked',
    defaultMessage: 'Unranked'
  },
  ReferralDetailsFriendsJoined: {
    id: 'memberReferralDetailsStats.friendsJoined',
    defaultMessage: 'Friends joined'
  },
  ReferralDetailsNrveEarned: {
    id: 'memberReferralDetailsStats.nrveEarned',
    defaultMessage: 'NRVE earned'
  }
});
