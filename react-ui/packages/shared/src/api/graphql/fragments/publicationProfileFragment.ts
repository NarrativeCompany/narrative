import gql from 'graphql-tag';
import { UserFragment } from './userFragment';

export const PublicationProfileFragment = gql`
  fragment PublicationProfile on PublicationProfile {
    oid
  
    creationDatetime
    followerCount
    canCurrentUserAppeal
    
    admins @type(name: "User") {
      ...User
    }
    editors @type(name: "User") {
      ...User
    }
    writers @type(name: "User") {
      ...User
    }

    contentRewardWriterShare
    contentRewardRecipient
  }
  ${UserFragment}
`;
