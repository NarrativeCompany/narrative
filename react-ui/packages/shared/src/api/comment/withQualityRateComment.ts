import { graphql } from 'react-apollo';
import { qualityRateCommentMutation } from '../graphql/comment/qualityRateCommentMutation';
import {
  CommentQueryFieldsInput,
  QualityRateCommentMutation,
  QualityRateCommentMutation_qualityRateComment,
  QualityRateCommentMutationVariables,
  QualityRatingInput
} from '../../types';
import { mutationResolver } from '../../utils';
import { WithCommentPostingProps, WithCommentQueryProps } from './commentQueryConstants';

const functionName = 'qualityRateComment';

type ParentProps =
  WithCommentPostingProps &
  WithCommentQueryProps;

export interface WithQualityRateCommentProps {
  [functionName]: (input: QualityRatingInput, queryField: CommentQueryFieldsInput) =>
    Promise<QualityRateCommentMutation_qualityRateComment>;
}

export const withQualityRateComment =
  graphql<
    ParentProps,
    QualityRateCommentMutation,
    QualityRateCommentMutationVariables,
    WithQualityRateCommentProps
  >(qualityRateCommentMutation, {
    props: ({mutate}) => ({
      [functionName]: async ( input: QualityRatingInput, queryFields: CommentQueryFieldsInput) => {
        return await mutationResolver<QualityRateCommentMutation>(mutate, {
          variables: { input, queryFields }
        }, functionName);
      }
    })
  });
