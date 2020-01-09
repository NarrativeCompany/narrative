import { graphql } from 'react-apollo';
import { ageRatePostMutation } from '../graphql/post/ageRatePostMutation';
import {
  AgeRatePostMutation,
  AgeRatePostMutation_ageRatePost,
  AgeRatePostMutationVariables,
  AgeRatingInput,
} from '../../types';
import { mutationResolver } from '../../utils';

const functionName = 'ageRatePost';

export interface WithAgeRatePostProps {
  [functionName]: (input: AgeRatingInput, postOid: string) => Promise<AgeRatePostMutation_ageRatePost>;
}

export const withAgeRatePost =
  graphql<
    {},
    AgeRatePostMutation,
    AgeRatePostMutationVariables,
    WithAgeRatePostProps
  >(ageRatePostMutation, {
    props: ({mutate}) => ({
      [functionName]: async (input: AgeRatingInput, postOid: string) => {
        return await mutationResolver<AgeRatePostMutation>(mutate, {
          variables: { input, postOid }
        }, functionName);
      }
    })
  });
