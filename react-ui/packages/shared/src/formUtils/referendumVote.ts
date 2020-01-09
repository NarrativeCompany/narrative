import * as yup from 'yup';
import { ObjectSchema } from 'yup';
import { ReferendumVoteReason } from '../types';

export interface ReferendumVoteFormValues {
  reason?: ReferendumVoteReason;
  comment?: string;
}

export const referendumVoteInitialValues: ReferendumVoteFormValues = {
  reason: undefined,
  comment: undefined
};

const reason = yup
  .mixed()
  .oneOf([
    ReferendumVoteReason.SPELLING_ISSUE_IN_NAME,
    ReferendumVoteReason.VIOLATES_TOS,
    ReferendumVoteReason.CONTAINS_PROFANITY,
    ReferendumVoteReason.REDUNDANT,
    ReferendumVoteReason.UNCLEAR_NAME_OR_DESCRIPTION,
    ReferendumVoteReason.WRONG_LANGUAGE,
  ], 'Reason is a required field')
  .required();
const comment = yup.string();

export const referendumVoteSchema: ObjectSchema<ReferendumVoteFormValues> = yup.object({reason, comment});

export const referendumVoteFormikUtil = {
  validationSchema: referendumVoteSchema,
  mapPropsToValues: (defaultValues: ReferendumVoteFormValues) => ({...referendumVoteInitialValues, ...defaultValues})
};
