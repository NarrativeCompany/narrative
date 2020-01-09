import { branch, compose, renderComponent, withProps } from 'recompose';
import { AuthenticationState, withAuthState, WithAuthStateProps } from '@narrative/shared';

export interface WithExtractedAuthStateProps {
  userAuthenticated: boolean;
  userRequires2FA: boolean;
  userNotAuthenticated: boolean;
  authStateLoading: boolean;
}

export const withExtractedAuthState = compose(
  withAuthState,
  branch((props: WithAuthStateProps) => props.authStateData && props.authStateData.loading,
    renderComponent(() => null)
  ),
  withProps((props: WithAuthStateProps) => {
    const { authStateData } = props;
    const authState = authStateData.authState;

    const authStateLoading = authStateData.loading;
    const userAuthenticated =
      authState &&
      authState.authenticationState &&
      authState.authenticationState === AuthenticationState.USER_AUTHENTICATED;
    const userRequires2FA =
      authState &&
      authState.authenticationState &&
      authState.authenticationState === AuthenticationState.USER_REQUIRES_2FA;
    const userNotAuthenticated =
      authState &&
      authState.authenticationState &&
      authState.authenticationState === AuthenticationState.USER_NOT_AUTHENTICATED;

    return { userAuthenticated, userRequires2FA, userNotAuthenticated, authStateLoading };
  })
);
