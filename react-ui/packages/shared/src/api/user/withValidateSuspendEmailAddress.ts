import { graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import {
  SuspendEmailInput,
  SuspendEmailValidation,
  ValidateSuspendEmailAddressQuery,
  ValidateSuspendEmailAddressQueryVariables
} from '../../types';
import { validateSuspendEmailAddressQuery } from '../graphql/user/validateSuspendEmailAddressQuery';
import { LoadingProps } from '../../utils';

export interface WithValidateSuspendEmailAddressParentProps {
  input: SuspendEmailInput;
  userOid: string;
}

export interface WithValidateSuspendEmailAddressProps extends LoadingProps {
  validationResult: SuspendEmailValidation;
}

type Props =
  NamedProps<
    {validateSuspendEmailAddressData: GraphqlQueryControls & ValidateSuspendEmailAddressQuery},
    WithValidateSuspendEmailAddressParentProps
  >;

export const withValidateSuspendEmailAddress =
  graphql<
    WithValidateSuspendEmailAddressParentProps,
    ValidateSuspendEmailAddressQuery,
    ValidateSuspendEmailAddressQueryVariables,
    WithValidateSuspendEmailAddressProps
    >(validateSuspendEmailAddressQuery, {
    options: ({input, userOid}) => ({
      variables: {
        input, userOid
      }
    }),
    name: 'validateSuspendEmailAddressData',
    props: ({ validateSuspendEmailAddressData }: Props): WithValidateSuspendEmailAddressProps => {
      const { loading, validateSuspendEmailAddress } = validateSuspendEmailAddressData;
      const validationResult =
        validateSuspendEmailAddress &&
        validateSuspendEmailAddress;

      return { loading, validationResult };
    }
  });
