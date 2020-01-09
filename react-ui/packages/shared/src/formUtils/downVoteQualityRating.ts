import * as yup from 'yup';
import { ObjectSchema } from 'yup';
import { QualityRating } from '../types';

export interface DownVoteQualityRatingFormValues {
  rating?: QualityRating;
  reason?: string;
}

const downVoteQualityRatingInitialValues: DownVoteQualityRatingFormValues = {
  rating: undefined,
  reason: undefined
};

const rating = yup
  .mixed()
  .oneOf([
    QualityRating.DISLIKE_LOW_QUALITY_CONTENT,
    QualityRating.DISLIKE_CONTENT_VIOLATES_AUP,
    QualityRating.DISLIKE_DISAGREE_WITH_VIEWPOINT,
  ], 'Reason is a required field')
  .required();

const reason = yup.string();

export const downVoteQualityRatingSchema: ObjectSchema<DownVoteQualityRatingFormValues> =
  yup.object({rating, reason});

export const downVoteQualityRatingFormikUtil = {
  validationSchema: downVoteQualityRatingSchema,
  mapPropsToValues: (defaultValues: DownVoteQualityRatingFormValues) =>
    ({...downVoteQualityRatingInitialValues, ...defaultValues})
};
