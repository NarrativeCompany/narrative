import { graphql } from 'react-apollo';
import { FetchResult } from 'apollo-link';
import {
  ImmediateFiatPaymentInput,
  PostImmediateFiatPaymentMutation,
  PostImmediateFiatPaymentMutation_postImmediateFiatPayment
} from '../../types';
import { postImmediateFiatPaymentMutation } from '../graphql/invoice/postImmediateFiatPaymentMutation';
import { resolveExceptionFromApolloError } from '../../utils';

export interface WithPostImmediateFiatPaymentProps {
  postImmediateFiatPayment: (input: ImmediateFiatPaymentInput)
    => Promise<PostImmediateFiatPaymentMutation_postImmediateFiatPayment>;
}

export const withPostImmediateFiatPayment = graphql(postImmediateFiatPaymentMutation, {
  props: ({mutate}) => ({
    postImmediateFiatPayment: async (input: ImmediateFiatPaymentInput) => {
      const variables = { input };
      const options = {
        variables,
        mutation: postImmediateFiatPaymentMutation
      };

      if (!mutate) {
        throw new Error ('withPostImmediateFiatPayment: missing mutate');
      }

      return mutate(options)
        .then((response: FetchResult<PostImmediateFiatPaymentMutation>) => {
          const postImmediateFiatPayment =
            response &&
            response.data &&
            response.data.postImmediateFiatPayment;

          if (!postImmediateFiatPayment) {
            throw new Error(
              'withPostImmediateFiatPayment: no return value from postImmediateFiatPayment mutation'
            );
          }

          return postImmediateFiatPayment;
        }).catch((error) => {
          throw resolveExceptionFromApolloError(error);
        });
    }
  })
});
