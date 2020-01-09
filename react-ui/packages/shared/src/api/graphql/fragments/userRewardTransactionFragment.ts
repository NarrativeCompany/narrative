import gql from 'graphql-tag';
import { NrveUsdValueFragment } from './nrveUsdValueFragment';
import { PostFragment } from './postFragment';
import { NicheFragment } from './nicheFragment';
import { UserFragment } from './userFragment';

export const UserRewardTransactionFragment = gql`
  fragment UserRewardTransaction on UserRewardTransaction {
    oid
    transactionDatetime
    status
    type
    metadataUser @type(name: "User") {
      ...User
    }
    metadataNiche @type(name: "Niche") {
      ...Niche
    }
    metadataPost @type(name: "Post") {
      ...Post
    }
    metadataContentCreatorRewardRole
    metadataActivityBonusPercentage
    metadataNeoWalletAddress
    metadataNeoTransactionId
    memo
    amount @type(name: "NrveUsdValue") {
      ...NrveUsdValue
    }
    
  }
  ${UserFragment}
  ${NicheFragment}
  ${PostFragment}
  ${NrveUsdValueFragment}
`;
