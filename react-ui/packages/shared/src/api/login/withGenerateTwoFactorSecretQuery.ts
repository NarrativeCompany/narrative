import { graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { generateTwoFactorSecretQuery } from '../graphql/login/generateTwoFactorSecretQuery';
import { GenerateTwoFactorSecretQuery } from '../../types';

export type WithGenerateTwoFactorSecretProps =
  NamedProps<{twoFactorSecretData: GraphqlQueryControls & GenerateTwoFactorSecretQuery}, {}>;

export const withGenerateTwoFactorSecret =
  graphql<{}, GenerateTwoFactorSecretQuery, {}>(generateTwoFactorSecretQuery, {
  name: 'twoFactorSecretData',
});
