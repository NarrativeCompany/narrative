import * as React from 'react';
import { compose } from 'recompose';
import { NicheList } from '../components/NicheList';
import { List } from 'antd';
import { AuctionCard } from './AuctionCard';
import { NotFound } from '../../../shared/components/NotFound';
import { WebRoute } from '../../../shared/constants/routes';
import { NicheAuction, withAllAuctionItems, WithAllAuctionItemsProps } from '@narrative/shared';
import {
  withPaginationController,
  WithPaginationControllerProps
} from '../../../shared/containers/withPaginationController';
import { generateSkeletonListProps, renderLoadingCard } from '../../../shared/utils/loadingUtils';

type Props =
  WithAllAuctionItemsProps &
  WithPaginationControllerProps ;

const ActiveAuctionListPageComponent: React.SFC<Props> = (props) => {
  const { loading, auctions, pagination, pageSize } = props;

  if (loading) {
    return (
      <NicheList {...generateSkeletonListProps(pageSize, renderLoadingCard)}/>
    );
  } else if (!auctions || !auctions.length) {
    return <NotFound/>;
  } else {
    return (
      <NicheList
        dataSource={auctions}
        pagination={pagination}
        renderItem={(auction: NicheAuction) => (
          <List.Item key={auction.oid}>
            <AuctionCard auction={auction}/>
          </List.Item>
        )}
      />
    );
  }
};

export const ActiveAuctionListPage = compose(
  withPaginationController<WithAllAuctionItemsProps>(
    withAllAuctionItems,
    WebRoute.AuctionsActive
  )
)(ActiveAuctionListPageComponent) as React.ComponentClass<{}>;
