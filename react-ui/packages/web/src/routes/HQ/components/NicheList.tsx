import * as React from 'react';
import { List } from 'antd';
import { ListProps } from 'antd/lib/list';

type Props =
  ListProps;

export const NicheList: React.SFC<Props> = (props: Props) => (
  <List
    grid={{gutter: 32, xs: 1, sm: 1, md: 2, lg: 2, xl: 3}}
    dataSource={props.dataSource}
    renderItem={props.renderItem}
    {...props}
  />
);
