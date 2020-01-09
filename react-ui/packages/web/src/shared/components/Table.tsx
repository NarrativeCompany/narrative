import * as React from 'react';
import { Table as AntTable } from 'antd';
import { TableProps } from 'antd/lib/table';
import styled, { css } from '../styled';

const TableComponent = styled<TableProps<{}>>((props) => <AntTable {...props}/>)`
  .ant-table {
    background: #fff;
    border: 1px solid ${props => props.theme.borderGrey};
    border-radius: 4px;
    
    tr {
      td {
        border-color: ${props => props.theme.borderGrey};
      }
    
      &:last-child td {
        border-bottom: none !important;
      } 
    }
  }
  
  .ant-table-thead {
    text-transform: uppercase;
    font-size: ${props => props.theme.textFontSizeSmall};
    
    th {
      color: ${props => props.theme.textColorLight};
      background: #fff;
      border-color: ${props => props.theme.borderGrey};
    }
  }
  
  // bl: antd shows a placeholder envelope image with "No Data" text while the table data is loading,
  // which doesn't look very good. so, let's hide those while loading. intentionally using visbility:hidden
  // instead of display:none so that the elements themselves still take up space. that way, the loading spinner
  // appears more centered in the table data and the "No Data" will display inline in the event that there
  // is actually no data loaded from the server.
  ${props => props.loading && css`
    .ant-empty-image, .ant-empty-description {
      visibility: hidden;
    }
  `}
` as React.ComponentType as new <T>() => AntTable<T>;

export function Table<T> (props: TableProps<T>) {
  return (
    <TableComponent<T> {...props}/>
  );
}
