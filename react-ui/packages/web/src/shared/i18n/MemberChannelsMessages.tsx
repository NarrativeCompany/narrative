import { defineMessages } from 'react-intl';

export const MemberChannelsMessages = defineMessages({
  NichesTab: {
    id: 'memberChannels.nichesTab',
    defaultMessage: 'Niches'
  },
  PublicationsTab: {
    id: 'memberChannels.publicationsTab',
    defaultMessage: 'Publications'
  },
  NichesTitle: {
    id: 'memberNiches.title',
    defaultMessage: '{displayName} - Niches'
  },
  MemberNichesDescription: {
    id: 'memberNichesCurrentUserIntro.myNichesDescription',
    defaultMessage: 'You have used {nicheCountHtml} of your available Niche slots.'
  },
  AcquireFirstNiche: {
    id: 'memberNichesCurrentUserIntro.acquireMoreNiches',
    defaultMessage: '{suggestLink}, {reviewLink} or {bidLink} on a Niche to acquire one.'
  },
  AcquireMoreNiches: {
    id: 'memberNichesCurrentUserIntro.acquireMoreNiches',
    defaultMessage: '{suggestLink}, {reviewLink} or {bidLink} on a Niche to acquire more.'
  },
  Suggest: {
    id: 'memberNichesCurrentUserIntro.suggest',
    defaultMessage: 'Suggest'
  },
  Review: {
    id: 'memberNichesCurrentUserIntro.review',
    defaultMessage: 'review'
  },
  Bid: {
    id: 'memberNichesCurrentUserIntro.bid',
    defaultMessage: 'bid'
  },
  NoNiches: {
    id: 'memberNicheLists.noNiches',
    defaultMessage: '{displayName} currently has no Niches.'
  },
  NichesYouOwn: {
    id: 'memberAssociatedNiches.nichesYouOwn',
    defaultMessage: 'Niches you own...'
  },
  NichesUserOwns: {
    id: 'memberAssociatedNiches.nichesUserOwns',
    defaultMessage: 'Niches {displayName} owns...'
  },
  NichesYoureBiddingOn: {
    id: 'memberAssociatedNiches.nichesYoureBiddingOn',
    defaultMessage: 'Niches you’re bidding on...'
  },
  NichesUserIsBiddingOn: {
    id: 'memberAssociatedNiches.nichesUserIsBiddingOn',
    defaultMessage: 'Niches {displayName} is bidding on...'
  },
  Purchased: {
    id: 'memberAssociatedNiche.purchased',
    defaultMessage: 'Purchased {purchaseDatetime}'
  },
  PublicationsTitle: {
    id: 'memberPublications.publicationsTitle',
    defaultMessage: '{displayName} - Publications'
  },
  PublicationsYouAreAssociatedWith: {
    id: 'memberPublicationsBody.publicationsYouAreAssociatedWith',
    defaultMessage: '{publicationsLink} you are involved with...'
  },
  PublicationsMemberAssociatedWith: {
    id: 'memberPublicationsBody.publicationsYourAssociatedWith',
    defaultMessage: '{publicationsLink} {displayName} is involved with...'
  },
  YouHaveNoPublicationAssociations: {
    id: 'memberPublicationsDescription.youHaveNoPublicationAssociations',
    defaultMessage: 'You are not involved with any {publicationsLink}.'
  },
  MemberHasNoPublicationAssociations: {
    id: 'memberPublicationsDescription.memberHasNoPublicationAssociations',
    defaultMessage: '{displayName} is not involved with any {publicationsLink}.'
  },
  OwnerRoleName: {
    id: 'memberAssociatedPublication.ownerRoleName',
    defaultMessage: 'Owner'
  },
});
