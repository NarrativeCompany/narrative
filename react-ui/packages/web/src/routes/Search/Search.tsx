import * as React from 'react';
import { RouteComponentProps, withRouter } from 'react-router-dom';
import { compose, withProps } from 'recompose';
import {
  getQueryArg,
  SearchType,
  withState,
  WithStateProps
} from '@narrative/shared';
import { SEO } from '../../shared/components/SEO';
import { SearchMessages } from '../../shared/i18n/SearchMessages';
import { FormattedMessage, injectIntl, InjectedIntlProps } from 'react-intl';
import { WebRoute } from '../../shared/constants/routes';
import styled, { css } from '../../shared/styled';
import { Form as FormikForm } from 'formik';
import { Input, Select } from 'antd';
import { ViewWrapper } from '../../shared/components/ViewWrapper';
import { SearchResults } from './SearchResults';
import { createUrl } from '../../shared/utils/routeUtils';
import { EnhancedSearchType } from '../../shared/enhancedEnums/searchType';
import { mediaQuery } from '../../shared/styled/utils/mediaQuery';

export const searchInputContainerBaseCss = css`
  margin: 0 auto 20px;
  max-width: 800px;
  width: 100%;
`;

const SearchInputContainer = styled(FormikForm)`
  ${searchInputContainerBaseCss}

  // jw: not sure why, but the selector seems to be one pixel below the input
  .ant-input-group div.ant-select {
    width: 20%;
    margin-top: -1px;
  }

  .ant-input-search {
    width: 80%;
  }

  // jw: Due to the compexity of the search input the outer group CSS is not properly styling the border on this, so
  //     lets do it for it.
  input {
    border-top-left-radius: 0;
    border-bottom-left-radius: 0;
  }
  
  ${mediaQuery.sm_down`
    .ant-input-group div.ant-select {
      width: 30%;
    }
  
    .ant-input-search {
      width: 70%;
    }
  `}
`;

export interface SearchParamProps {
  keyword?: string;
  filter?: SearchType;
  channelOid?: string;
  lastResultIndex?: number;
  forLoadMore?: boolean;
}

interface State {
  selectedFilter: SearchType;
}

type Props =
  InjectedIntlProps &
  RouteComponentProps<{}> &
  WithStateProps<State> &
  SearchParamProps & {
    selectedFilter: SearchType;
  };

const SearchComponent: React.SFC<Props> = (props) => {
  const { keyword, filter, intl: { formatMessage }, setState, state: { selectedFilter } } = props;

  return (
    <React.Fragment>
      <SEO
        title={SearchMessages.Title_SEO}
        description={SearchMessages.Description_SEO}
        robots="noindex,nofollow"
      />
      <ViewWrapper>
        <SearchInputContainer>
          <Input.Group compact={true}>
            <Select
              size="large"
              value={selectedFilter}
              onChange={(value: SearchType) => setState(ss => ({...ss, selectedFilter: value}))}
            >
              {EnhancedSearchType.enhancers.map((searchType) => (
                <Select.Option key={searchType.type} value={searchType.type}>
                  <FormattedMessage {...searchType.titleMessage}/>
                </Select.Option>
              ))}
            </Select>
            <Input.Search
              autoFocus={true}
              defaultValue={keyword}
              size="large"
              placeholder={formatMessage(SearchMessages.Placeholder)}
              onSearch={(value, e) => {
                const selectedFilterType = EnhancedSearchType.get(selectedFilter);
                props.history.push(createUrl(WebRoute.Search, {
                  keyword: value,
                  filter: selectedFilterType.getUrlParamValue()
                }));
                // jw: if we do not prevent defaults, the redirect above will be shorted out due to browser navigation
                //     that takes place due to the carriage return falling through.
                if (e) {
                  e.preventDefault();
                }
              }}
              enterButton={true}
            />
          </Input.Group>
        </SearchInputContainer>

        {keyword && <SearchResults keyword={keyword} filter={filter} />}
      </ViewWrapper>
    </React.Fragment>
  );
};

export const Search = compose(
  withRouter,
  withState<State, RouteComponentProps<{}>>((props) => {
    // jw: let's pull the initial selectedFilter out of the URL so that the selector reflects the search.
    const { location: { search } } = props;

    const filterValue = getQueryArg(search, 'filter');
    let selectedFilter: SearchType = filterValue ? SearchType[filterValue] : SearchType.everything;
    // jw: in case we ever change the names for these let's go ahead and catch where the filterValue lookup failed to
    //     match above and default to everything.
    if (!selectedFilter) {
      selectedFilter = SearchType.everything;
    }

    return { selectedFilter };
  }),
  withProps((props: RouteComponentProps<{}>) => {
    const { location: { search } } = props;
    // jw: Because people can add anything to a url, let's only parse the things that we expect, since parseQueryArgs
    //     does not take a signature, we need to be safe.
    const keyword = getQueryArg(search, 'keyword');
    const filter = getQueryArg(search, 'filter');

    return { keyword, filter };
  }),
  injectIntl
)(SearchComponent) as React.ComponentClass<{}>;
