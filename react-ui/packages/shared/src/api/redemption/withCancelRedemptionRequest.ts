import { graphql } from 'react-apollo';
import {
  CancelRedemptionRequestInput,
  CancelRedemptionRequestMutation_cancelRedemptionRequest,
  CancelRedemptionRequestMutation,
  CancelRedemptionRequestMutationVariables
} from '../../types';
import { mutationResolver } from '../../utils';
import { cancelRedemptionRequestMutation } from '../graphql/redemption/cancelRedemptionRequestMutation';

const functionName = 'cancelRedemptionRequest';

export interface WithCancelRedemptionRequestProps {
  [functionName]: (input: CancelRedemptionRequestInput) =>
    Promise<CancelRedemptionRequestMutation_cancelRedemptionRequest>;
}

export const withCancelRedemptionRequest =
  graphql<
    {},
    CancelRedemptionRequestMutation,
    CancelRedemptionRequestMutationVariables,
    WithCancelRedemptionRequestProps
  >(cancelRedemptionRequestMutation, {
    props: ({mutate}) => ({
      [functionName]: async (input: CancelRedemptionRequestInput) => {
        return await mutationResolver<CancelRedemptionRequestMutation>(mutate, {
          variables: { input }
        }, functionName);
      }
    })
  });
