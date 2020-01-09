import { defineMessages } from 'react-intl';

export const MemberFollowsMessages = defineMessages({
  Following: {
    id: 'memberFollows.following',
    defaultMessage: 'Following'
  },
  Followers: {
    id: 'memberFollows.followers',
    defaultMessage: 'Followers'
  },
  FollowersSeoTitle: {
    id: 'memberFollows.followersSeoTitle',
    defaultMessage: '{displayName} - Followers'
  },
  FollowersSeoDescription: {
    id: 'memberFollows.followersSeoDescription',
    defaultMessage: 'All of the people who are following {displayName}.'
  },
  Niches: {
    id: 'memberFollowedItems.niches',
    defaultMessage: 'Niches'
  },
  NichesSeoTitle: {
    id: 'memberFollowedItems.nichesSeoTitle',
    defaultMessage: '{displayName} - Followed Niches'
  },
  NichesSeoDescription: {
    id: 'memberFollows.nichesSeoDescription',
    defaultMessage: 'All of the Niches that {displayName} is following.'
  },
  NoFollowedNiches: {
    id: 'memberFollows.noFollowedNiches',
    defaultMessage: '{displayName} is not following any Niches.'
  },
  NoFollowedNichesForCurrentUser: {
    id: 'memberFollows.noFollowedNichesForCurrentUser',
    defaultMessage: 'You are not following any Niches.'
  },
  Publications: {
    id: 'memberFollowedItems.publications',
    defaultMessage: 'Publications'
  },
  PublicationsSeoTitle: {
    id: 'memberFollowedItems.publicationsSeoTitle',
    defaultMessage: '{displayName} - Followed Publications'
  },
  PublicationsSeoDescription: {
    id: 'memberFollows.publicaitonsSeoDescription',
    defaultMessage: 'All of the Publications that {displayName} is following.'
  },
  NoFollowedPublications: {
    id: 'memberFollows.noFollowedPublications',
    defaultMessage: '{displayName} is not following any Publications.'
  },
  NoFollowedPublicationsForCurrentUser: {
    id: 'memberFollows.noFollowedPublicationsForCurrentUser',
    defaultMessage: 'You are not following any Publications.'
  },
  People: {
    id: 'memberFollowedItems.people',
    defaultMessage: 'People'
  },
  PeopleSeoTitle: {
    id: 'memberFollowedItems.peopleSeoTitle',
    defaultMessage: '{displayName} - Followed People'
  },
  PeopleSeoDescription: {
    id: 'memberFollows.peopleSeoDescription',
    defaultMessage: 'All of the people that {displayName} is following.'
  },
  NoFollowedPeople: {
    id: 'memberFollows.noFollowedPeople',
    defaultMessage: '{displayName} is not following anyone.'
  },
  NoFollowedPeopleForCurrentUser: {
    id: 'memberFollows.noFollowedPeopleForCurrentUser',
    defaultMessage: 'You are not following anyone.'
  },
  YouHaveFollowers: {
    id: 'memberFollowers.youHaveFollowers',
    defaultMessage: 'You have {totalFollowers, number} {totalFollowers, plural, one {follower} other {followers}}.'
  },
  MemberHasFollowers: {
    id: 'memberFollowers.memberHasFollowers',
    defaultMessage: '{displayName} has {totalFollowers, number} {totalFollowers, plural, one {follower} other ' +
      '{followers}}.'
  },
  MemberHasFollowersHidden: {
    id: 'memberFollowers.memberHasFollowersHidden',
    defaultMessage: '{displayName} has {totalFollowers, number} {totalFollowers, plural, one {follower} other ' +
      '{followers}} but prefers not to share the list.'
  },
  MemberHasFollowsHidden: {
    id: 'MemberFollowedItems.memberHasFollowsHidden',
    defaultMessage: '{displayName} prefers not to share these lists.'
  },
  ChangeSetting: {
    id: 'followListHiddenWarning.changeSetting',
    defaultMessage: 'Change setting?'
  },
  RestrictingLists: {
    id: 'followListHiddenWarning.restrictingLists',
    defaultMessage: 'You are not allowing anyone else to see this list. {changeSetting}'
  },
});
