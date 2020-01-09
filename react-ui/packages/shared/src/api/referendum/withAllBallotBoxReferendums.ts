import { ChildDataProps, graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { allBallotBoxReferendumsQuery } from '../graphql/referendum/allBallotBoxReferendumsQuery';
import { AllBallotBoxReferendumsQuery, ExtractedPageableProps, Referendum } from '../../types';
import { getPageableQueryProps } from '../../utils';

const defaultPageSize = 15;
const queryName = 'ballotBoxData';
const functionName = 'getAllBallotBoxReferendums';

interface ParentProps {
  currentPage: number;
}

type WithProps = NamedProps<
  {[queryName]: GraphqlQueryControls & AllBallotBoxReferendumsQuery},
  WithAllBallotBoxReferendumsProps
>;

export type WithAllBallotBoxReferendumsProps =
  ChildDataProps<ParentProps, AllBallotBoxReferendumsQuery> &
  ExtractedPageableProps & {
  referendums: Referendum[]
};

export const withAllBallotBoxReferendums =
  graphql<
    ParentProps,
    AllBallotBoxReferendumsQuery,
    {},
    WithAllBallotBoxReferendumsProps
  >(allBallotBoxReferendumsQuery, {
    skip: (ownProps: ParentProps) => !ownProps.currentPage,
    options: (ownProps: ParentProps) => ({
      variables: {
        size: defaultPageSize,
        page: ownProps.currentPage - 1
      }
    }),
    name: queryName,
    props: ({ ballotBoxData , ownProps }: WithProps) => {
      const { items, ...extractedProps } =
        getPageableQueryProps<AllBallotBoxReferendumsQuery, Referendum>(ballotBoxData, functionName);

      return {
        ...ownProps,
        ...extractedProps,
        referendums: items,
        pageSize: defaultPageSize,
      };
    }
  });
