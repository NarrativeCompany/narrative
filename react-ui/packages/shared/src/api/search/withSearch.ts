import { graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { searchQuery } from '../graphql/search/searchQuery';
import { SearchQuery, SearchType } from '../../types';
import { infiniteLoadingFixProps } from '../../utils';

export interface WithSearchParentProps {
  keyword: string;
  filter: SearchType;
  channelOid?: string;
  count?: number;
  startIndex?: number;
}

export type WithSearchProps =
  NamedProps<{searchData: GraphqlQueryControls & SearchQuery}, {}>;

export const withSearch = graphql<WithSearchParentProps, SearchQuery>(searchQuery, {
  // jw: let's only process the search if we have a keyword to run it with.
  skip: ({keyword}) => !keyword,
  options: (props) => ({
    ...infiniteLoadingFixProps,
    variables: {
      input: {
        keyword: props.keyword,
        filter: props.filter,
        // jw: note: the parameter the server receives is different, but I want the ParentProp to be more explicit.
        channel: props.channelOid,
        count: props.count,
        startIndex: props.startIndex
      }
    }
  }),
  name: 'searchData'
});
