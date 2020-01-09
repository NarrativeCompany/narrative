import { graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import {
  ValidateResetPasswordUrlInput,
  ValidateResetPasswordUrlQuery,
  ValidateResetPasswordUrlQueryVariables
} from '../../types';
import { validateResetPasswordUrlQuery } from '../graphql/user/validateResetPasswordUrlQuery';

interface ParentProps {
  input: ValidateResetPasswordUrlInput;
  userOid: string;
}

export type WithValidateResetPasswordUrlProps =
  NamedProps<{validateResetPasswordUrlData: GraphqlQueryControls & ValidateResetPasswordUrlQuery}, ParentProps>;

export const withValidateResetPasswordUrl =
  graphql<
    ParentProps,
    ValidateResetPasswordUrlQuery,
    ValidateResetPasswordUrlQueryVariables
    >(validateResetPasswordUrlQuery, {
    options: ({input, userOid}) => ({
      variables: {
        input, userOid
      }
    }),
    name: 'validateResetPasswordUrlData'
  });
