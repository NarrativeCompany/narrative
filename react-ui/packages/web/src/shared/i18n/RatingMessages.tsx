import { defineMessages } from 'react-intl';

export const RatingMessages = defineMessages({
  LikeContent: {
    id: 'qualityRating.like',
    defaultMessage: 'Like Content'
  },
  LowQualityContent: {
    id: 'qualityRating.lowQualityContent',
    defaultMessage: 'Low Quality Content'
  },
  ContentViolatesAup: {
    id: 'qualityRating.contentViolatesAup',
    defaultMessage: 'Content Violates {aupLink}, which includes:'
  },
  DisagreeWithViewpoint: {
    id: 'qualityRating.disagreeWithViewpoint',
    defaultMessage: 'Disagree with Viewpoint'
  },
  RatePostsAction: {
    id: 'ratingMessages.ratePostsAction',
    defaultMessage: 'rate posts'
  },
  RateCommentsAction: {
    id: 'ratingMessages.rateComments',
    defaultMessage: 'rate comments'
  },
  ReasonForDownvote: {
    id: 'DislikeRatingSelectorModal.reasonForDownvote',
    defaultMessage: 'Reason for Downvote'
  },
  Reason: {
    id: 'DislikeRatingSelectorModal.reason',
    defaultMessage: 'Reason'
  },
  Vote: {
    id: 'DislikeRatingSelectorModal.vote',
    defaultMessage: 'Vote'
  },
  CannotRateOwnPost: {
    id: 'cannotRateOwnContentWarning.cannotRateOwnPost',
    defaultMessage: 'You cannot rate your own post!'
  },
  CannotRateOwnComment: {
    id: 'cannotRateOwnContentWarning.cannotRateOwnComment',
    defaultMessage: 'You cannot rate your own comment!'
  },
  RatingsDisabled: {
    id: 'ratingDisabledWarning.ratingsDisabled',
    defaultMessage: 'Ratings are disabled until the post is approved.'
  },
  VoteCount: {
    id: 'postQualityRating.voteCount',
    defaultMessage: '{voteCount} {votes, plural, one {vote} other {votes}}'
  },
  AllAges: {
    id: 'ageRating.allAges',
    defaultMessage: 'All Ages'
  },
  EighteenPlus: {
    id: 'ageRating.eighteenPlus',
    defaultMessage: '18+'
  },
  Audience: {
    id: 'postAgeRating.audience',
    defaultMessage: 'Audience'
  },
  Rating: {
    id: 'postQualityRating.rating',
    defaultMessage: 'Rating'
  },
  Edit: {
    id: 'authorAgeRatePostWarning.edit',
    defaultMessage: 'edit your post'
  },
  AuthorAgeRateWarning: {
    id: 'authorAgeRatePostWarning.message',
    defaultMessage: 'If you’d like to edit your audience classification, {editLink}.'
  },
  NotWrittenInEnglish: {
    id: 'downVoteQualityRatingSelectorModal.notWrittenInEnglish',
    defaultMessage: 'Not Written in English'
  },
  CopyrightInfringement: {
    id: 'downVoteQualityRatingSelectorModal.copyrightInfringement',
    defaultMessage: 'Copyright Infringement'
  },
  IllegalActivities: {
    id: 'downVoteQualityRatingSelectorModal.illegalActivities',
    defaultMessage: 'Illegal Activities'
  },
  Pornography: {
    id: 'downVoteQualityRatingSelectorModal.pornography',
    defaultMessage: 'Pornography'
  },
});
