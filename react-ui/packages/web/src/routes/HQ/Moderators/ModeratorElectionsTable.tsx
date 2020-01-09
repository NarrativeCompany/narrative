import * as React from 'react';
import { Table } from '../../../shared/components/Table';
import { Paragraph } from '../../../shared/components/Paragraph';
import { LocalizedTime } from '../../../shared/components/LocalizedTime';
import { ViewElectionButton } from './ViewElectionButton';
import { NicheLink } from '../../../shared/components/niche/NicheLink';
import { FormattedMessage } from 'react-intl';
import { ColumnProps } from 'antd/lib/table';
import { ElectionListProps } from './ModeratorElectionsListPage';
import { ModeratorElectionsMessages } from '../../../shared/i18n/ModeratorElectionsMessages';
import { NicheModeratorElection } from '@narrative/shared';
import styled from '../../../shared/styled';

const TableWrapper = styled.div`
  @media screen and (max-width: 767px) {
    display: none;
  }
`;

// tslint:disable array-type
const columns: ColumnProps<NicheModeratorElection>[] = [
  {
    title: <FormattedMessage {...ModeratorElectionsMessages.NicheColumnTitle}/>,
    dataIndex: 'niche',
    render: (_: undefined, electionItem: NicheModeratorElection) =>
      <NicheLink niche={electionItem.niche} weight={600}>{electionItem.niche.name}</NicheLink>
  },
  {
    title: <FormattedMessage {...ModeratorElectionsMessages.OpeningsColumnTitle}/>,
    dataIndex: 'openings',
    width: '15%',
    render: (_: undefined, electionItem: NicheModeratorElection) =>
      <Paragraph>{electionItem.election.availableSlots}</Paragraph>
  },
  {
    title: <FormattedMessage {...ModeratorElectionsMessages.NomineesColumnTitle}/>,
    dataIndex: 'nominees',
    width: '15%',
    render: (_: undefined, electionItem: NicheModeratorElection) =>
      <Paragraph>{electionItem.election.nomineeCount}</Paragraph>
  },
  {
    title: <FormattedMessage {...ModeratorElectionsMessages.PurchaseDateColumnTitle}/>,
    dataIndex: 'purchaseDate',
    width: '20%',
    render: (_: undefined, electionItem: NicheModeratorElection) =>
      <LocalizedTime time={electionItem.election.nominationStartDatetime}/>
  },
  {
    align: 'right',
    dataIndex: 'viewElection',
    width: '10%',
    render: (_: undefined, electionItem: NicheModeratorElection) =>
      <ViewElectionButton electionOid={electionItem.election.oid}/>
  }
];

export const ModeratorElectionsTable: React.SFC<ElectionListProps> = (props) => {
  const { electionItems, loading, pagination } = props;

  return (
    <TableWrapper>
      <Table<NicheModeratorElection>
        loading={loading}
        pagination={pagination}
        rowKey={data => data.oid}
        dataSource={electionItems}
        columns={columns}
      />
    </TableWrapper>
  );
};
