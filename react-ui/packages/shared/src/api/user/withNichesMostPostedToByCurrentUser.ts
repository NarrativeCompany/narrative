import { ChildDataProps, graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { nichesMostPostedToByCurrentUserQuery } from '../graphql/user/nichesMostPostedToByCurrentUserQuery';
import { Niche, NichesMostPostedToByCurrentUserQuery } from '../../types';

const queryName = 'nichesMostPostedToData';

type WithProps = NamedProps<
  {[queryName]: GraphqlQueryControls & NichesMostPostedToByCurrentUserQuery},
  WithNichesMostPostedToByCurrentUserProps
>;

export type WithNichesMostPostedToByCurrentUserProps =
  ChildDataProps<{}, NichesMostPostedToByCurrentUserQuery> & {
  recentlyUsedNiches: Niche[];
};

export const withNichesMostPostedToByCurrentUser =
  graphql<
    {},
    NichesMostPostedToByCurrentUserQuery,
    {},
    WithNichesMostPostedToByCurrentUserProps
  >(nichesMostPostedToByCurrentUserQuery, {
    name: queryName,
    props: ({ nichesMostPostedToData, ownProps }: WithProps) => {
      const loading = nichesMostPostedToData.loading;
      const recentlyUsedNiches = extractMostPostedNiches(nichesMostPostedToData) as Niche[];

      return { ...ownProps, loading, recentlyUsedNiches };
    }
  });

function extractMostPostedNiches (data: GraphqlQueryControls & NichesMostPostedToByCurrentUserQuery) {
  return data && data.getNichesMostPostedToByCurrentUser || [];
}
