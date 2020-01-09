import * as React from 'react';
import { compose, withHandlers, withProps } from 'recompose';
import {
  withState,
  WithStateProps,
  User,
  SearchResult,
  WithSearchParentProps,
  SearchType,
  withSearch,
  WithSearchProps
} from '@narrative/shared';
import { Select } from 'antd';
import { FormattedMessage } from 'react-intl';
import { SearchInput } from '../niche/NicheSearch';
import { SharedComponentMessages } from '../../i18n/SharedComponentMessages';
import { FormControl } from '../FormControl';
import { Text } from '../Text';

interface State {
  searchString?: string;
}

interface ParentProps {
  onMemberSelected: (user: User) => void;
}

interface Handlers {
  handleSelectChange: (userOid: string) => void;
}

interface Props extends WithStateProps<State>, Handlers {
  searchResults: User[];
}

const MemberSearchComponent: React.SFC<Props> = (props) => {
  const { searchResults, handleSelectChange, state: { searchString }, setState } = props;

  const options = searchResults.map((user: User) => (
    <Select.Option key={user.oid} value={user.oid}>
      {user.displayName}{' '}
      <Text color="light">@{user.username}</Text>
    </Select.Option>
  ));

  return (
    <FormControl>
      <SearchInput
        placeholder={<FormattedMessage {...SharedComponentMessages.MemberSearchPlaceholder}/>}
        showSearch={true}
        showArrow={false}
        defaultActiveFirstOption={false}
        filterOption={false}
        value={searchString}
        onSearch={value => setState(ss => ({ ...ss, searchString: value && value.length ? value : undefined }))}
        onChange={(value: string) => handleSelectChange(value)}
        style={{width: '100%'}}
      >
        {options}
      </SearchInput>
    </FormControl>
  );
};

export const MemberSearch = compose(
  withState<State>({}),
  // jw: first, we need to put together the props for searching
  withProps<WithSearchParentProps, WithStateProps<State>>((props: WithStateProps<State>) => {
    const keyword = props.state.searchString || '';

    return {
      keyword,
      filter: SearchType.members,
      count: 10
    };
  }),
  // jw: next, we need to run the search. Note: the search will skip if the keyword does not have a value, so no
  //     cost in doing this each time.
  withSearch,
  // jw: finally, we need to parse the search results.
  withProps<Pick<Props, 'searchResults'>, WithSearchProps>((props: WithSearchProps) => {
    const { searchData } = props;

    // jw: because the withSearch could skip if we did not have a keyword we need to check for the existence of the
    //     searchData which is not something we traditionally have to do.
    const results = searchData &&
      searchData.getSearchResults &&
      searchData.getSearchResults.items || [];

    const searchResults: User[] = [];
    results.forEach((result: SearchResult) => {
      const { userDetail } = result;
      if (userDetail) {
        searchResults.push(userDetail.user);
      }
    });

    return { searchResults };
  }),
  withHandlers<Props & ParentProps, Handlers>({
    handleSelectChange: (props) => (userOid: string) => {
      const { searchResults, setState, onMemberSelected } = props;

      const user = searchResults.find(u => u.oid === userOid);

      if (user) {
        setState(ss => ({ ...ss, searchString: undefined }));
        onMemberSelected(user);
      }
    }
  })
)(MemberSearchComponent) as React.ComponentClass<ParentProps>;
