import * as React from 'react';
import { compose } from 'recompose';
import { AuthenticationState, withState, WithStateProps } from '@narrative/shared';
import { getAuthTokenState, removeAuthToken, storeAuthToken } from '../utils/authTokenUtils';

// tslint:disable no-any
export const AuthContext: React.Context<any> = React.createContext(undefined as any);

interface AuthActionProps {
  login: (token: string) => any;
  logout: () => any;
}
// tslint:enable no-any

interface AuthValueState {
  isAuthenticated: boolean;
}

const initialState: AuthValueState = {
  isAuthenticated: AuthenticationState.USER_AUTHENTICATED === getAuthTokenState().getAuthenticationState(),
};

type AuthContextProps =
  WithStateProps<AuthValueState>;

const AuthProviderComponent: React.SFC<AuthContextProps> = (props) => {
  const { state, setState, children } = props;

  return (
    <AuthContext.Provider value={{
      isAuthenticated: state.isAuthenticated,
      login: (token: string) => {
        // FIXME - AuthStore should no longer be used
        storeAuthToken(token, true);
        setState(ss => ({...ss,
          isAuthenticated: AuthenticationState.USER_AUTHENTICATED === getAuthTokenState().getAuthenticationState()}));
      },
      logout: () => {
        removeAuthToken();
        setState(ss => ({...ss, isAuthenticated: false}));
      }
    }}>
      {children}
    </AuthContext.Provider>
  );
};

type AuthProviderProps =
  AuthValueState &
  AuthActionProps;

export const AuthProvider = compose(
  withState<AuthValueState>(initialState),
)(AuthProviderComponent) as React.ComponentClass<{}>;

export interface AuthStoreProps {
  authStoreValues: AuthValueState;
  authStoreActions: AuthActionProps;
}

export const AuthConnect = <P extends AuthStoreProps>(
  WrappedComponent: React.ComponentType<P> | React.SFC<P>
) => {
  return class extends React.PureComponent {
    public render () {
      return (
        <AuthContext.Consumer>
          {({isAuthenticated, login, logout}: AuthProviderProps) => {
            return (
              <WrappedComponent
                authStoreValues={{isAuthenticated}}
                authStoreActions={{login, logout}}
                {...this.props}
              />
            );
          }}
        </AuthContext.Consumer>
      );
    }
  };
};
