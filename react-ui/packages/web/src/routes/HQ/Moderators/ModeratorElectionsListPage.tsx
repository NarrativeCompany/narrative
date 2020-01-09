import * as React from 'react';
import { compose } from 'recompose';
import { ModeratorElectionsTable } from './ModeratorElectionsTable';
import { ModeratorElectionsCards } from './ModeratorElectionsCards';
import { WebRoute } from '../../../shared/constants/routes';
import { PaginationProps } from 'antd/lib/pagination';
import {
  NicheModeratorElection,
  withNicheModeratorElections,
  WithNicheModeratorElectionsProps
} from '@narrative/shared';
import {
  withPaginationController,
  WithPaginationControllerProps
} from '../../../shared/containers/withPaginationController';

export interface ElectionListProps {
  electionItems: NicheModeratorElection[];
  loading: boolean;
  pagination: PaginationProps;
  pageSize: number;
}

type Props =
  WithNicheModeratorElectionsProps &
  WithPaginationControllerProps;

export const ModeratorElectionsListPageComponent: React.SFC<Props> = (props) => {
  const { pagination, pageSize, loading, electionItems } = props;

  return (
    <React.Fragment>
      <ModeratorElectionsTable
        electionItems={electionItems}
        loading={loading}
        pagination={pagination}
        pageSize={pageSize}
      />

      <ModeratorElectionsCards
        loading={loading}
        electionItems={electionItems}
        pagination={pagination}
        pageSize={pageSize}
      />
    </React.Fragment>
  );
};

export const ModeratorElectionsListPage = compose(
  withPaginationController<WithNicheModeratorElectionsProps>(
    withNicheModeratorElections,
    WebRoute.ModeratorElections
  )
)(ModeratorElectionsListPageComponent) as React.ComponentClass<{}>;
