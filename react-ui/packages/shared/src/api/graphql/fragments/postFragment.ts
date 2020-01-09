import gql from 'graphql-tag';
import { UserFragment } from './userFragment';
import { NicheFragment } from './nicheFragment';
import { PublicationFragment } from './publicationFragment';
import { QualityRatingFieldsFragment } from './qualityRatingFieldsFragment';
import { AgeRatingFieldsFragment } from './ageRatingFieldsFragment';

export const PostFragment = gql`
  fragment Post on Post {
    oid
    title
    subTitle
    author @type(name: "User") {
      ...User
    }
    prettyUrlString
    postLive
    moderationDatetime
    liveDatetime
    lastUpdateDatetime
    lastSaveDatetime
    titleImageUrl
    titleImageLargeUrl
    titleImageLargeWidth
    titleImageLargeHeight
    titleImageSquareUrl
    publishedToPersonalJournal
    publishedToPublication @type(name: "Publication") {
      ...Publication
    }
    publishedToNiches @type(name: "Niche") {
      ...Niche
    }
    qualityRatingFields @type(name: "QualityRatingFields") {
      ...QualityRatingFields
    }
    ageRatingFields @type(name: "AgeRatingFields") {
      ...AgeRatingFields
    }
    featuredInPublication
  }
  ${UserFragment}
  ${PublicationFragment}
  ${NicheFragment}
  ${QualityRatingFieldsFragment}
  ${AgeRatingFieldsFragment}
`;
