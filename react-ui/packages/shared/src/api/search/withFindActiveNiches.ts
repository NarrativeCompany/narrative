import { ChildDataProps, graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { findActiveNichesQuery } from '../graphql/search/findActiveNichesQuery';
import { FindActiveNichesQuery, Niche } from '../../types';

const queryName = 'findActiveNichesData';
const count: number = 25;

interface ParentProps {
  name: string;
}

type WithProps = NamedProps<
  {[queryName]: GraphqlQueryControls & FindActiveNichesQuery},
  WithFindActiveNichesProps
>;

export type WithFindActiveNichesProps =
  ChildDataProps<ParentProps, FindActiveNichesQuery> & {
  activeNiches: Niche[];
  loading: boolean;
};

export const withFindActiveNiches =
  graphql<
    ParentProps,
    FindActiveNichesQuery,
    {},
    WithFindActiveNichesProps
  >(findActiveNichesQuery, {
    skip: ({ name }: ParentProps) => !name,
    options: ({ name }: ParentProps) => ({
      variables: {
        input: { name, count }
      }
    }),
    props: ({ findActiveNichesData, ownProps }: WithProps) => {
      const loading = findActiveNichesData.loading;
      const activeNiches = extractActiveNicheResults(findActiveNichesData) as Niche[];

      return { ...ownProps, loading, activeNiches };
    },
    name: queryName
  });

function extractActiveNicheResults (data: GraphqlQueryControls & FindActiveNichesQuery) {
  return data && data.findActiveNiches || [];
}
