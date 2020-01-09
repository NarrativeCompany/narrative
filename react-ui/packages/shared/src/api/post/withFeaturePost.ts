import { graphql } from 'react-apollo';
import { featurePostMutation } from '../graphql/post/featurePostMutation';
import { FeaturePostMutation, FeaturePostMutationVariables, FeaturedPostInput } from '../../types';
import { mutationResolver } from '../../utils';

const functionName = 'featurePost';

export interface WithFeaturePostProps {
  [functionName]: (input: FeaturedPostInput, postOid: string) => Promise<FeaturePostMutation>;
}

export const withFeaturePost =
  graphql<
    {},
    FeaturePostMutation,
    FeaturePostMutationVariables,
    WithFeaturePostProps
  >(featurePostMutation, {
    props: ({mutate}) => ({
      [functionName]: async (input: FeaturedPostInput, postOid: string) => {
        return await mutationResolver<FeaturePostMutation>(mutate, {
          variables: { input, postOid }
        }, functionName);
      }
    })
  });
