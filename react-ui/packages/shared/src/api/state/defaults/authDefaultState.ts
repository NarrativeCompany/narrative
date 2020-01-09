import { AuthenticationState } from '../../../types';

export const authDefaultState = {
  authState: {
    __typename: 'AuthState',
    authenticationState: AuthenticationState.USER_NOT_AUTHENTICATED
  }
};
