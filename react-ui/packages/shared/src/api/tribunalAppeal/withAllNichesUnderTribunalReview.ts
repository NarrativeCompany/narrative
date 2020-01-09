import { ChildDataProps, graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { allNichesUnderTribunalReviewQuery } from '../graphql/tribunalAppeal/allNichesUnderTribunalReviewQuery';
import { AllNichesUnderTribunalReviewQuery, ExtractedPageableProps, TribunalIssue } from '../../types';
import { getPageableQueryProps } from '../../utils';

const defaultPageSize = 10;
const queryName = 'allNichesUnderTribunalReviewData';
const functionName = 'getAllNichesUnderTribunalReview';

interface ParentProps {
  currentPage: number;
}

type WithProps = NamedProps<
  {[queryName]: GraphqlQueryControls & AllNichesUnderTribunalReviewQuery},
  WithAllNichesUnderTribunalReviewProps
>;

export type WithAllNichesUnderTribunalReviewProps =
  ChildDataProps<ParentProps, AllNichesUnderTribunalReviewQuery> &
  ExtractedPageableProps & {
  issues: TribunalIssue[];
};

export const withAllNichesUnderTribunalReview =
  graphql<
    ParentProps,
    AllNichesUnderTribunalReviewQuery,
    {},
    WithAllNichesUnderTribunalReviewProps
  >(allNichesUnderTribunalReviewQuery, {
    options: (ownProps: ParentProps) => ({
      variables: {
        size: defaultPageSize,
        page: ownProps.currentPage - 1
      }
    }),
    name: queryName,
    props: ({ allNichesUnderTribunalReviewData, ownProps }: WithProps) => {
      const { items, ...extractedProps } =
        getPageableQueryProps<
          AllNichesUnderTribunalReviewQuery,
          TribunalIssue
        >(allNichesUnderTribunalReviewData, functionName);

      return {
        ...ownProps,
        ...extractedProps,
        issues: items,
        pageSize: defaultPageSize
      };
    }
});
