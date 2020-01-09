import gql from 'graphql-tag';
import { UserRewardTransactionFragment } from '../fragments/userRewardTransactionFragment';
import { PageInfoFragment } from '../fragments/pageInfoFragment';

export const userRewardTransactionsQuery = gql`  
  query UserRewardTransactionsQuery($userOid: String!, $input: UserRewardTransactionsInput!) {
    getUserRewardTransactions (userOid: $userOid, input: $input)
    @rest(type: "UserRewardTransactionsPageData", path: "/users/{args.userOid}/reward-transactions?{args.input}") {
      items @type(name: "UserRewardTransaction") {
        ...UserRewardTransaction
      }
      info @type(name: "PageInfo") {
        ...PageInfo
      }
    }
  }
  ${UserRewardTransactionFragment}
  ${PageInfoFragment}
`;
