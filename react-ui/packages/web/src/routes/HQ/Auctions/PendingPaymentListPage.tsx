import * as React from 'react';
import { compose, withProps } from 'recompose';
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

interface WithProps {
  pendingPayment: boolean;
}

type Props =
  WithAllAuctionItemsProps &
  WithProps &
  WithPaginationControllerProps;

const PendingPaymentListPageComponent: React.SFC<Props> = (props) => {
  const { loading, pagination, pageSize, auctions } = props;

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
            <AuctionCard auction={auction} pendingPayment={true}/>
          </List.Item>
        )}
      />
    );
  }
};

export const PendingPaymentListPage = compose(
  withProps(() => ({
    pendingPayment: true,
  })),
  withPaginationController<WithAllAuctionItemsProps & WithProps>(
    withAllAuctionItems,
    WebRoute.AuctionsPendingPayment
  ),
)(PendingPaymentListPageComponent) as React.ComponentClass<{}>;
