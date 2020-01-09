import * as React from 'react';
import { List } from 'antd';
import { ExtractedPageableProps, TribunalIssue } from '@narrative/shared';
import { TribunalAppealsListItem } from './TribunalAppealsListItem';
import { NotFound } from '../../../shared/components/NotFound';
import { WithPaginationControllerProps } from '../../../shared/containers/withPaginationController';
import { generateSkeletonListProps, renderLoadingCard } from '../../../shared/utils/loadingUtils';

type ParentProps =
  ExtractedPageableProps &
  WithPaginationControllerProps & {
  issues: TribunalIssue[];
};

export const TribunalAppealsBaseListComponent: React.SFC<ParentProps> = (props) => {
  const { issues, loading, pagination, pageSize } = props;
  let ListContent;

  if (loading) {
    ListContent = (
      <List
        grid={{ gutter: 16, xxl: 1 }}
        {...generateSkeletonListProps(pageSize, renderLoadingCard)}
      />
    );
  } else if (!issues || !issues.length) {
    ListContent = <NotFound/>;
  } else {
    ListContent = (
      <List
        grid={{ gutter: 16, xxl: 1 }}
        dataSource={issues}
        pagination={pagination}
        renderItem={(issue: TribunalIssue) => (
          <List.Item key={issue.oid}>
            <TribunalAppealsListItem issue={issue}/>
          </List.Item>
        )}
      />
    );
  }

  return ListContent;
};
