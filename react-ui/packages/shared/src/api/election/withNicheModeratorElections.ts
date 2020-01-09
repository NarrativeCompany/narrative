import { ChildDataProps, graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { nicheModeratorElectionsQuery } from '../graphql/election/nicheModeratorElectionsQuery';
import { ExtractedPageableProps, NicheModeratorElection, NicheModeratorElectionsQuery } from '../../types';
import { getPageableQueryProps } from '../../utils';

const defaultPageSize: number = 20;
const queryName = 'nicheModeratorElectionsData';
const functionName = 'getNicheModeratorElections';

interface ParentProps {
  currentPage: number;
}

type WithProps = NamedProps<
  {[queryName]: GraphqlQueryControls & NicheModeratorElectionsQuery},
  WithNicheModeratorElectionsProps
>;

export type WithNicheModeratorElectionsProps =
  ChildDataProps<ParentProps, NicheModeratorElectionsQuery> &
  ExtractedPageableProps & {
  electionItems: NicheModeratorElection[];
};

export const withNicheModeratorElections =
  graphql<
    ParentProps,
    NicheModeratorElectionsQuery,
    {},
    WithNicheModeratorElectionsProps
  >(nicheModeratorElectionsQuery, {
    skip: (ownProps: ParentProps) => !ownProps.currentPage,
    options: (ownProps: ParentProps) => ({
      variables: {
        size: defaultPageSize,
        page: ownProps.currentPage - 1
      }
    }),
    name: queryName,
    props: ({ nicheModeratorElectionsData, ownProps }: WithProps) => {
      const { items, ...extractedProps } =
        getPageableQueryProps<
          NicheModeratorElectionsQuery,
          NicheModeratorElection
        >(nicheModeratorElectionsData, functionName);

      return {
        ...ownProps,
        ...extractedProps,
        electionItems: items,
        pageSize: defaultPageSize
      };
    }
});
