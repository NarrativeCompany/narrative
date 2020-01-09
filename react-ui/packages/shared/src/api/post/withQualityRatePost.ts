import { graphql } from 'react-apollo';
import { qualityRatePostMutation } from '../graphql/post/qualityRatePostMutation';
import {
  QualityRatePostMutation,
  QualityRatePostMutation_qualityRatePost,
  QualityRatePostMutationVariables,
  QualityRatingInput,
} from '../../types';
import { mutationResolver } from '../../utils';

const functionName = 'qualityRatePost';

export interface WithQualityRatePostProps {
  [functionName]: (input: QualityRatingInput, postOid: string) => Promise<QualityRatePostMutation_qualityRatePost>;
}

export const withQualityRatePost =
  graphql<
    {},
    QualityRatePostMutation,
    QualityRatePostMutationVariables,
    WithQualityRatePostProps
  >(qualityRatePostMutation, {
    props: ({mutate}) => ({
      [functionName]: async (input: QualityRatingInput, postOid: string) => {
        return await mutationResolver<QualityRatePostMutation>(mutate, {
          variables: { input, postOid }
        }, functionName);
      }
    })
  });
