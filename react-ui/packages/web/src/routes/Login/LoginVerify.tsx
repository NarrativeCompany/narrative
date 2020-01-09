import * as React from 'react';
import * as H from 'history';
import LoginForm from './LoginForm';
import { logout } from '../../shared/utils/authTokenUtils';
import { compose, withHandlers } from 'recompose';
import { RouteComponentProps } from 'react-router';
import { LocationType, resolvePreviousLocation } from '../../shared/utils/routeUtils';
import { WebRoute } from '../../shared/constants/routes';

export interface LoginVerifyHistoryState {
  returnTo?: LocationType;
}

interface Handlers {
  cancelAuthentication: () => void;
}

const LoginVerifyComponent: React.SFC<Handlers> = (props) => {
  const { cancelAuthentication } = props;

  return (
    <LoginForm
      dismiss={cancelAuthentication}
      forceTwoFactorVisible={true}
      showTwoFactorExpiredMessage={true}
    />
  );
};

export function getPreviousLocationFor2FAVerify(location: H.Location<LoginVerifyHistoryState>): LocationType {
  // jw: let's favor a location specified by the GlobalErrorController when it redirected here.
  if (location.state && location.state.returnTo) {
    return location.state.returnTo;
  }

  // jw: otherwise, let's try to derive one from the location history, falling back to home if all else fails.
  return resolvePreviousLocation(location, WebRoute.Home, WebRoute.SigninVerify);
}

export default compose(
  withHandlers<RouteComponentProps<{}, {}, LoginVerifyHistoryState>, Handlers>({
    cancelAuthentication: (props) => async () => {
      const { location } = props;

      const to = getPreviousLocationFor2FAVerify(location);

      // wj: first, we need to log the user out and redirect them to where they belong.
      await logout(to);
    }
  })
)(LoginVerifyComponent) as React.ComponentClass<{}>;
