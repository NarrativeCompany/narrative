import * as React from 'react';
import { compose, withProps } from 'recompose';
import { SearchParamProps } from './Search';
import { InjectedIntlProps, injectIntl } from 'react-intl';
import { SearchMessages } from '../../shared/i18n/SearchMessages';
import { withSearch, WithSearchProps } from '@narrative/shared';
import { fullPlaceholder, withLoadingPlaceholder } from '../../shared/utils/withLoadingPlaceholder';
import { SearchResultList } from './SearchResultList';
import { SearchResult } from '@narrative/shared';
import { FormattedMessage } from 'react-intl';

interface ParentProps extends SearchParamProps {
  noResultsMessageOverride?: FormattedMessage.MessageDescriptor;
  hideTypeLabels?: boolean;
}

export interface SearchResultsProps extends ParentProps {
  results: SearchResult[];
  includeLoadMore: boolean;
}

const SearchResultsComponent: React.SFC<SearchResultsProps> = (props) => {
  const { keyword } = props;

  // jw: if we do not have a keyword, let's short out.
  if (!keyword) {
    return null;
  }

  return <SearchResultList {...props} />;
};

export const SearchResults = compose(
  withSearch,
  // jw: we need this to setup the loadingPlaceholderTip
  injectIntl,
  withProps((props: WithSearchProps & InjectedIntlProps) => {
    const { searchData, intl } = props;
    const { loading, getSearchResults } = searchData;

    const results = getSearchResults &&
      getSearchResults.items || [];
    const startIndex = getSearchResults &&
      getSearchResults.lastResultIndex;

    // jw: include the load more button if we have more items
    const includeLoadMore = getSearchResults &&
      getSearchResults.hasMoreItems;

    const loadingPlaceholderTip = intl.formatMessage(SearchMessages.Action_Searching);

    return { results, startIndex, includeLoadMore, loading, loadingPlaceholderTip };
  }),
  withLoadingPlaceholder(fullPlaceholder)
)(SearchResultsComponent) as React.ComponentClass<ParentProps>;
