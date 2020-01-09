import * as React from 'react';
import { List } from 'antd';
import { Card } from '../../../shared/components/Card';
import { LocalizedTime } from '../../../shared/components/LocalizedTime';
import { LabelAndValue } from '../../../shared/components/LabelAndValue';
import { NotFound } from '../../../shared/components/NotFound';
import { ElectionListProps } from './ModeratorElectionsListPage';
import { CardListLoading } from '../../../shared/components/Loading';
import { ViewElectionButton } from './ViewElectionButton';
import { NicheLink } from '../../../shared/components/niche/NicheLink';
import { ListGridType } from 'antd/lib/list';
import { ModeratorElectionsMessages } from '../../../shared/i18n/ModeratorElectionsMessages';
import { NicheModeratorElection } from '@narrative/shared';
import styled from '../../../shared/styled';

const listGrid: ListGridType = { gutter: 16, xs: 1, sm: 2 };

const ListWrapper = styled.div`
  @media screen and (min-width: 768px) {
    display: none;
  }
`;

export const ModeratorElectionsCards: React.SFC<ElectionListProps> = (props) => {
  const { electionItems, loading, pagination, pageSize } = props;
  let ListContent;

  if (loading) {
    ListContent = <CardListLoading grid={listGrid} listLength={pageSize}/>;
  } else if (!electionItems || !electionItems.length) {
    ListContent = <NotFound/>;
  } else {
    ListContent = (
      <List
        grid={listGrid}
        dataSource={electionItems}
        pagination={pagination}
        renderItem={(electionItem: NicheModeratorElection) => {
          const { niche, election } = electionItem;

          return (
            <List.Item>
              <Card title={<NicheLink niche={niche} weight={600}>{niche.name}</NicheLink>}>
                <LabelAndValue label={ModeratorElectionsMessages.OpeningsColumnTitle}>
                  {election.availableSlots}
                </LabelAndValue>

                <LabelAndValue label={ModeratorElectionsMessages.NomineesColumnTitle}>
                  {election.nomineeCount}
                </LabelAndValue>

                <LabelAndValue label={ModeratorElectionsMessages.PurchaseDateColumnTitle}>
                  <LocalizedTime time={election.nominationStartDatetime}/>
                </LabelAndValue>

                <ViewElectionButton
                  buttonProps={{size: 'large', block: true}}
                  electionOid={election.oid}
                />
              </Card>
            </List.Item>
          );
        }}
      />
    );
  }

  return (
    <ListWrapper>
      {ListContent}
    </ListWrapper>
  );
};
