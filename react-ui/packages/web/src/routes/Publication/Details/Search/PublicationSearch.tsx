import * as React from 'react';
import { compose, withProps } from 'recompose';
import { RouteComponentProps, withRouter, generatePath } from 'react-router';
import { getQueryArg, SearchType } from '@narrative/shared';
import { injectIntl, InjectedIntlProps } from 'react-intl';
import {
  WithPublicationDetailsContextProps
} from '../../components/PublicationDetailsContext';
import styled from '../../../../shared/styled';
import { SEO } from '../../../../shared/components/SEO';
import { Input } from 'antd';
import { createUrl, getIdForUrl } from '../../../../shared/utils/routeUtils';
import { WebRoute } from '../../../../shared/constants/routes';
import { SearchResults } from '../../../Search/SearchResults';
import { PublicationDetailsMessages } from '../../../../shared/i18n/PublicationDetailsMessages';
import { searchInputContainerBaseCss } from '../../../Search/Search';
import { withExpiredPublicationError } from '../../components/withExpiredPublicationError';

interface Props extends WithPublicationDetailsContextProps, RouteComponentProps, InjectedIntlProps {
  keyword?: string;
}

const SearchInputContainer = styled.div`
  ${searchInputContainerBaseCss}
`;

const PublicationSearchComponent: React.SFC<Props> = (props) => {
  const { keyword, history, publicationDetail: { oid: channelOid, publication }, intl: { formatMessage } } = props;

  const id = getIdForUrl(publication.prettyUrlString, publication.oid);

  return (
    <React.Fragment>
      <SEO
        title={PublicationDetailsMessages.SearchSeoTitle}
        publication={publication}
        robots="noindex,nofollow"
      />
      <SearchInputContainer>
        <Input.Search
          autoFocus={true}
          defaultValue={keyword}
          size="large"
          placeholder={formatMessage(PublicationDetailsMessages.PublicationSearchPlaceholder)}
          onSearch={(value, e) => {
            history.push(createUrl(generatePath(WebRoute.PublicationSearch, {id}), {keyword: value}));
            // jw: if we do not prevent defaults, the redirect above will be shorted out due to browser navigation
            //     that takes place due to the carriage return falling through.
            if (e) {
              e.preventDefault();
            }
          }}
          enterButton={true}
        />
      </SearchInputContainer>

      {keyword && <SearchResults
        keyword={keyword}
        filter={SearchType.posts}
        channelOid={channelOid}
        noResultsMessageOverride={PublicationDetailsMessages.NoSearchResultsMessage}
        hideTypeLabels={true}
      />}
    </React.Fragment>
  );
};

export default compose(
  withExpiredPublicationError(),
  withRouter,
  withProps((props: RouteComponentProps<{}>) => {
    const { location: { search } } = props;
    const keyword = getQueryArg(search, 'keyword');

    return { keyword };
  }),
  injectIntl
)(PublicationSearchComponent) as React.ComponentClass<{}>;
