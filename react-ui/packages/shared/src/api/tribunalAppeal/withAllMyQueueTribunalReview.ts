import { ChildDataProps, graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { allMyQueueTribunalReviewQuery } from '../graphql/tribunalAppeal/allMyQueueTribunalReviewQuery';
import { AllMyQueueTribunalReviewQuery, ExtractedPageableProps, TribunalIssue } from '../../types';
import { getPageableQueryProps } from '../../utils';

const defaultPageSize = 10;
const queryName = 'allMyQueueTribunalReviewData';
const functionName = 'getAllMyQueueTribunalReview';

interface ParentProps {
  currentPage: number;
}

type WithProps = NamedProps<
  {[queryName]: GraphqlQueryControls & AllMyQueueTribunalReviewQuery},
  WithAllMyQueueTribunalReviewProps
>;

export type WithAllMyQueueTribunalReviewProps =
  ChildDataProps<ParentProps & AllMyQueueTribunalReviewQuery> &
  ExtractedPageableProps & {
  issues: TribunalIssue[];
};

export const withAllMyQueueTribunalReview =
  graphql<
    ParentProps,
    AllMyQueueTribunalReviewQuery,
    {},
    WithAllMyQueueTribunalReviewProps
  >(allMyQueueTribunalReviewQuery, {
    skip: (ownProps: ParentProps) => !ownProps.currentPage,
    options: (ownProps: ParentProps) => ({
      variables: {
        size: defaultPageSize,
        page: ownProps.currentPage - 1
      }
    }),
    name: queryName,
    props: ({ allMyQueueTribunalReviewData, ownProps }: WithProps) => {
      const { items, ...extractedProps } =
        getPageableQueryProps<
          AllMyQueueTribunalReviewQuery,
          TribunalIssue
        >(allMyQueueTribunalReviewData, functionName);

      return {
        ...ownProps,
        ...extractedProps,
        issues: items,
        pageSize: defaultPageSize
      };
    }
});
