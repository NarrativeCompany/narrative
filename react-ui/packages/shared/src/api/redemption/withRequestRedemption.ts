import {
  RequestRedemptionInput,
  RequestRedemptionMutation,
  RequestRedemptionMutation_requestRedemption,
  RequestRedemptionMutationVariables
} from '../../types';
import { mutationResolver } from '../../utils';
import { requestRedemptionMutation } from '../graphql/redemption/requestRedemptionMutation';
import { graphql } from 'react-apollo';

const functionName = 'requestRedemption';

export interface WithRequestRedemptionProps {
  [functionName]: (input: RequestRedemptionInput) => Promise<RequestRedemptionMutation_requestRedemption>;
}

export const withRequestRedemption =
  graphql<
    {},
    RequestRedemptionMutation,
    RequestRedemptionMutationVariables,
    WithRequestRedemptionProps
  >(requestRedemptionMutation, {
    props: ({mutate}) => ({
      [functionName]: async (input: RequestRedemptionInput) => {
        return await mutationResolver<RequestRedemptionMutation>(mutate, {
          variables: { input }
        }, functionName);
      }
    })
  });
