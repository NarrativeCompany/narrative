import { ChildDataProps, graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import {
  allNichesWithCompletedTribunalReviewQuery
} from '../graphql/tribunalAppeal/allNichesWithCompletedTribunalReviewQuery';
import { AllNichesWithCompletedTribunalReviewQuery, ExtractedPageableProps, TribunalIssue } from '../../types';
import { getPageableQueryProps } from '../../utils';

const defaultPageSize = 10;
const queryName = 'allNichesWithCompletedTribunalReviewData';
const functionName = 'getAllNichesWithCompletedTribunalReview';

interface ParentProps {
  currentPage: number;
}

type WithProps = NamedProps<
  {[queryName]: GraphqlQueryControls & AllNichesWithCompletedTribunalReviewQuery},
  WithAllNichesWithCompletedTribunalReviewProps
>;

export type WithAllNichesWithCompletedTribunalReviewProps =
  ChildDataProps<ParentProps & AllNichesWithCompletedTribunalReviewQuery> &
  ExtractedPageableProps & {
  issues: TribunalIssue[];
};

export const withAllNichesWithCompletedTribunalReview =
  graphql<
    ParentProps,
    AllNichesWithCompletedTribunalReviewQuery,
    {},
    WithAllNichesWithCompletedTribunalReviewProps
  >(
    allNichesWithCompletedTribunalReviewQuery,
    {
      options: (ownProps: ParentProps) => ({
        variables: {
          size: defaultPageSize,
          page: ownProps.currentPage - 1
        }
      }),
      name: queryName,
      props: ({ allNichesWithCompletedTribunalReviewData, ownProps }: WithProps) => {
        const { items, ...extractedProps } =
          getPageableQueryProps<
            AllNichesWithCompletedTribunalReviewQuery,
            TribunalIssue
          >(allNichesWithCompletedTribunalReviewData, functionName);

        return {
          ...ownProps,
          ...extractedProps,
          issues: items,
          pageSize: defaultPageSize
        };
      }
    });
