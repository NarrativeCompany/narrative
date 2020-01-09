import * as React from 'react';
import { NotFound } from '../../shared/components/NotFound';
import { FormattedMessage } from 'react-intl';
import { SearchMessages } from '../../shared/i18n/SearchMessages';
import { SearchItem } from './SearchItem';
import { Col, List, Row } from 'antd';
import { LegacyLoadMoreButton } from '../../shared/components/LegacyLoadMoreButton';
import { SearchResults, SearchResultsProps } from './SearchResults';
import { CardLink } from '../../shared/components/CardLink';
import styled from '../../shared/styled';
import { SearchResult } from '@narrative/shared';

const CardLinkWrapper = styled.div`
  max-width: 728px;
  margin: 20px auto 0;
`;

export const SearchResultList: React.SFC<SearchResultsProps> = (props) => {
  const { results, forLoadMore, noResultsMessageOverride, hideTypeLabels } = props;

  // jw: if a keyword is defined, and we have no results, then that means there were no matches
  if (!results || !results.length) {
    // jw: just output nothing if this is for load more, since we had results before.
    if (forLoadMore) {
      return null;
    }

    // jw: since this was not for load more, let's include the Not Found response with the extra details for search.
    return (
      <React.Fragment>
        <NotFound/>
        <h1 style={{textAlign: 'center'}}>
          <FormattedMessage {...(noResultsMessageOverride || SearchMessages.Title_NotFound)}/>
        </h1>
        {/* jw: if we don't have a custom no results message then include the global nav cards */}
        {!noResultsMessageOverride &&
          <CardLinkWrapper>
            <Row gutter={28}>
              <Col xs={24} sm={12} style={{marginBottom: 28}}>
                <CardLink type="review"/>
              </Col>
              <Col xs={24} sm={12} style={{marginBottom: 28}}>
                <CardLink type="bid"/>
              </Col>
            </Row>
          </CardLinkWrapper>
        }
      </React.Fragment>
    );
  }

  // jw: the only thing left to do is render the results, and optionally include the load more button!
  const { includeLoadMore } = props;
  return (
    <React.Fragment>
      <List dataSource={results}
            renderItem={(searchResult: SearchResult) => (
              <SearchItem searchResult={searchResult} hideTypeLabel={hideTypeLabels} />
            )}
      />
      {includeLoadMore && <LegacyLoadMoreButton
        fetchMoreItems={() => {
          return <SearchResults {...props} forLoadMore={true} />;
        }}
      />}
    </React.Fragment>
  );
};
