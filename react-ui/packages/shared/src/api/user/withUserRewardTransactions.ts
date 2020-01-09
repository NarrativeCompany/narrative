import { graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import {
  ExtractedPageableProps,
  UserRewardTransaction,
  UserRewardTransactionsQuery,
  UserRewardTransactionsQueryVariables
} from '../../types';
import { userRewardTransactionsQuery } from '../graphql/user/userRewardTransactionsQuery';
import { getPageableQueryProps } from '../../utils';

const defaultPageSize = 20;
const queryName = 'userRewardTransactionsData';
const functionName = 'getUserRewardTransactions';

export interface UserTransactionsParentProps {
  userOid: string;
  currentPage: number;
}

export type WithExtractedUserRewardTransactionsProps =
  ExtractedPageableProps & {
  transactions: UserRewardTransaction[];
};

type WithUserRewardTransactionsProps = NamedProps<
  {[queryName]: GraphqlQueryControls & UserRewardTransactionsQuery},
  UserTransactionsParentProps
>;

export const withUserRewardTransactions =
  graphql<
    UserTransactionsParentProps,
    UserRewardTransactionsQuery,
    UserRewardTransactionsQueryVariables,
    WithExtractedUserRewardTransactionsProps
   >(userRewardTransactionsQuery, {
     name: queryName,
    skip: (ownProps: UserTransactionsParentProps) => !ownProps.currentPage,
    options: (props: UserTransactionsParentProps) => {
      const { userOid, currentPage } = props;
      return {
        variables: {
          userOid,
          input: {
            size: defaultPageSize,
            page: currentPage - 1
          }
        }
      };
    },
    props: ({ userRewardTransactionsData, ownProps }: WithUserRewardTransactionsProps) => {
       const { items, ...extractedProps } =
        getPageableQueryProps<UserRewardTransactionsQuery, UserRewardTransaction>
        (userRewardTransactionsData, functionName);

       return {
        ...ownProps,
        ...extractedProps,
        transactions: items,
        pageSize: defaultPageSize
      };
    }
  });
