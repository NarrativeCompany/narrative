import { graphql } from 'react-apollo';
import {
  placeSecurityDepositOnNicheAuctionMutation
} from '../graphql/auction/placeSecurityDepositOnNicheAuctionMutation';
import {
  FiatPaymentInput, NicheAuctionDetail,
  PlaceSecurityDepositOnNicheAuctionMutation,
  PlaceSecurityDepositOnNicheAuctionMutationVariables
} from '../../types';
import { mutationResolver } from '../../utils';

const functionName = 'placeSecurityDepositOnNicheAuction';

export interface WithPlaceSecurityDepositOnNicheAuctionProps {
  [functionName]: (input: FiatPaymentInput, auctionOid: string) => Promise<NicheAuctionDetail>;
}

export const withPlaceSecurityDepositOnNicheAuction =
  graphql<
    {},
    PlaceSecurityDepositOnNicheAuctionMutation,
    PlaceSecurityDepositOnNicheAuctionMutationVariables,
    WithPlaceSecurityDepositOnNicheAuctionProps
  >(placeSecurityDepositOnNicheAuctionMutation, {
    props: ({mutate}) => ({
      [functionName]: async (input: FiatPaymentInput, auctionOid: string) => {
        return await mutationResolver<PlaceSecurityDepositOnNicheAuctionMutation>(mutate, {
          variables: { input, auctionOid }
        }, functionName);
      }
    })
  });
