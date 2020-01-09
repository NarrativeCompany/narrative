import { ChildDataProps, graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { ApolloQueryResult, FetchMoreOptions, FetchMoreQueryOptions } from 'apollo-client';
import { electionNomineesQuery } from '../graphql/election/electionNomineesQuery';
import {
  ElectionNominee,
  ElectionNomineesQuery,
  ElectionNomineesQueryVariables,
  NicheModeratorElectionDetailsQuery
} from '../../types';
import { LoadingProps } from '../../utils';

interface ParentProps {
  lastItemDatetime: string | null;
  electionOid: string;
  count?: number;
}

interface WithExtractedElectionNomineesProps extends LoadingProps {
  electionNominees: ElectionNominee[];
  lastItemDatetime: string | null;
  hasMoreItems: boolean;
}

export type WithElectionNomineesProps =
  ChildDataProps<ParentProps, NicheModeratorElectionDetailsQuery> &
  GraphqlQueryControls &
  WithExtractedElectionNomineesProps;

type ElectionNomineesData = GraphqlQueryControls & ElectionNomineesQuery;

type WithProps = NamedProps<{electionNomineesData: ElectionNomineesData}, WithElectionNomineesProps>;

export const withElectionNominees =
  graphql<
    ParentProps,
    ElectionNomineesQuery,
    ElectionNomineesQueryVariables,
    WithElectionNomineesProps
  >(electionNomineesQuery, {
    // skip: ({ electionOid }: ParentProps) => !electionOid,
    options: ({ electionOid, count, lastItemDatetime}: ParentProps) => ({
      variables: {
        input: {
          confirmedBefore: lastItemDatetime,
          count: count || 12,
        },
        electionOid,
      }
    }),
    name: 'electionNomineesData',
    props: ({ electionNomineesData, ownProps }: WithProps) => {
      const extractedProps = getExtractedElectionNomineesProps(electionNomineesData);

      return {
        ...electionNomineesData,
        ...ownProps,
        ...extractedProps
      };
    }
  });

function getExtractedElectionNomineesProps (data: ElectionNomineesData): WithExtractedElectionNomineesProps {
  const { getElectionNominees } = data;
  const loading = data.loading;
  const electionNominees =
    getElectionNominees &&
    getElectionNominees.items || [];
  const lastItemDatetime =
    getElectionNominees &&
    getElectionNominees.lastItemConfirmationDatetime;
  const hasMoreItems =
    getElectionNominees &&
    getElectionNominees.hasMoreItems;

  return { loading, electionNominees, lastItemDatetime, hasMoreItems };
}

export const fetchMoreNominees =  async (
  variables: ElectionNomineesQueryVariables,
  // tslint:disable-next-line no-any
  fetchMore: (fetchMoreOptions: FetchMoreQueryOptions<any, any> & FetchMoreOptions) => Promise<ApolloQueryResult<any>>
) => {
  await fetchMore({
    variables,
    // prev represents our previous store value for the query before we 'fetched more'
    updateQuery: (prev, { fetchMoreResult }) => {
      // if we don't have any new results return the previous
      if (!fetchMoreResult) {
        return prev;
      }

      const prevExtractedProps = getExtractedElectionNomineesProps(prev);
      const newExtractedProps = getExtractedElectionNomineesProps(fetchMoreResult);

      // merge the fresh result to the previous and write the new values to the store
      return {
        getElectionNominees: {
          __typename: 'ElectionNominees',
          items: [
            ...prevExtractedProps.electionNominees,
            ...newExtractedProps.electionNominees
          ],
          hasMoreItems: newExtractedProps.hasMoreItems,
          lastItemConfirmationDatetime: newExtractedProps.lastItemDatetime
        }
      };
    }
  });
};
