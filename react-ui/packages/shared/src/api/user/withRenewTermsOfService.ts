import {
  RenewTermsOfServiceMutation,
  RenewTermsOfServiceMutation_renewTermsOfService,
  RenewTermsOfServiceMutationVariables
} from '../../types';
import { buildMutationImplFunction } from '../../utils';

import { renewTermsOfServiceMutation } from '../graphql/user/renewTermsOfServiceMutation';

export interface WithRenewTermsOfServiceProps {
  renewTermsOfService: (input: RenewTermsOfServiceMutationVariables) =>
    Promise<RenewTermsOfServiceMutation_renewTermsOfService>;
}

export const withRenewTermsOfService =
  buildMutationImplFunction<RenewTermsOfServiceMutationVariables, RenewTermsOfServiceMutation>(
    renewTermsOfServiceMutation,
    'renewTermsOfService'
  );
