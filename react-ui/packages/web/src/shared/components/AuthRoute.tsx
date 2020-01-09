import * as React from 'react';
import { compose } from 'recompose';
import { Redirect, Route, RouteProps } from 'react-router-dom';
import { WebRoute } from '../constants/routes';
import { withExtractedAuthState, WithExtractedAuthStateProps } from '../containers/withExtractedAuthState';

type ParentProps = RouteProps;

type Props =
  ParentProps &
  WithExtractedAuthStateProps;

export class AuthRouteComponent extends React.PureComponent<Props, {}> {
  constructor (props: Props) {
    super(props);
  }

  public render () {
    const { location, userAuthenticated } = this.props;

    if (!userAuthenticated) {
      return (
        <Redirect
          to={{
            pathname: WebRoute.Signin,
            state: {from: location}
          }}
        />
      );
    }

    return <Route {...this.props}/>;
  }
}

export const AuthRoute = compose(
  withExtractedAuthState
)(AuthRouteComponent) as React.ComponentClass<ParentProps>;
