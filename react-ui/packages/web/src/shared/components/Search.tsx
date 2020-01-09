import * as React from 'react';
import { Input } from 'antd';
import { SearchProps } from 'antd/lib/input';

const AntSearch = Input.Search;

type Props = SearchProps;

export const Search: React.SFC<Props> = (props) => {
  return (
    <AntSearch {...props}/>
  );
};