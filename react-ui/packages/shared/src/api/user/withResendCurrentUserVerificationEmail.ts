import {
  ResendCurrentUserVerificationEmailMutation,
  ResendCurrentUserVerificationEmailMutation_resendCurrentUserVerificationEmail,
  ResendCurrentUserVerificationEmailMutationVariables,
} from '../../types';
import { buildMutationImplFunction } from '../../utils';
import { resendCurrentUserVerificationEmailMutation } from '../graphql/user/resendCurrentUserVerificationEmailMutation';

export interface WithResendCurrentUserVerificationEmailProps {
  resendCurrentUserVerificationEmail: (input: ResendCurrentUserVerificationEmailMutationVariables) =>
    Promise<ResendCurrentUserVerificationEmailMutation_resendCurrentUserVerificationEmail>;
}

export const withResendCurrentUserVerificationEmail =
  buildMutationImplFunction<
    ResendCurrentUserVerificationEmailMutationVariables,
    ResendCurrentUserVerificationEmailMutation
    >(
  resendCurrentUserVerificationEmailMutation,
  'resendCurrentUserVerificationEmail'
);
