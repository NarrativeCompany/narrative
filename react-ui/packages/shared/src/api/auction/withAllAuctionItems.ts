import { ChildDataProps, graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { allAuctionItemsQuery } from '../graphql/auction/allAuctionItemsQuery';
import { AllAuctionItemsQuery, ExtractedPageableProps, NicheAuction } from '../../types';
import { getPageableQueryProps } from '../../utils';

const defaultPageSize = 15;
const queryName = 'auctionsData';
const functionName = 'getAllNicheAuctions';

interface ParentProps {
  currentPage: number;
  pendingPayment: boolean;
}

type WithProps = NamedProps<
  {[queryName]: GraphqlQueryControls & AllAuctionItemsQuery},
  WithAllAuctionItemsProps
>;

export type WithAllAuctionItemsProps =
  ChildDataProps<ParentProps, AllAuctionItemsQuery> &
  ExtractedPageableProps & {
  auctions: NicheAuction[];
};

export const withAllAuctionItems =
  graphql<
    ParentProps,
    AllAuctionItemsQuery,
    {},
    WithAllAuctionItemsProps
  >(allAuctionItemsQuery, {
    skip: (ownProps: ParentProps) => !ownProps.currentPage,
    options: (ownProps: ParentProps) => ({
      variables: {
        input: {
          size: defaultPageSize,
          page: ownProps.currentPage - 1,
          pendingPayment: ownProps.pendingPayment
        }
      }
    }),
    name: queryName,
    props: ({ auctionsData, ownProps }: WithProps) => {
      const { items, ...extractedProps } =
        getPageableQueryProps<AllAuctionItemsQuery, NicheAuction>(auctionsData, functionName);

      return {
        ...ownProps,
        ...extractedProps,
        auctions: items,
        pageSize: defaultPageSize,
      };
    }
  });
