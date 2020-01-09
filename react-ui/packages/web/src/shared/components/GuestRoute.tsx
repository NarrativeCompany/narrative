import * as React from 'react';
import { compose } from 'recompose';
import { Redirect, Route, RouteComponentProps, RouteProps, withRouter } from 'react-router-dom';
import { WebRoute } from '../constants/routes';
import { withExtractedAuthState, WithExtractedAuthStateProps } from '../containers/withExtractedAuthState';
import { resolvePreviousLocation } from '../utils/routeUtils';

type ParentProps = RouteProps;

type Props =
  ParentProps &
  WithExtractedAuthStateProps &
  RouteComponentProps<{}>;

export class GuestRouteComponent extends React.PureComponent<Props, {}> {
  constructor (props: Props) {
    super(props);
  }

  public render () {
    const { location, userAuthenticated } = this.props;

    if (userAuthenticated) {
      const prevLocation = resolvePreviousLocation(location);
      const returnLocation = prevLocation ? prevLocation : WebRoute.Home;

      return (
        <Redirect
          to={{
            pathname: returnLocation,
            state: {from: location}
          }}
        />
      );
    } else {
      return <Route {...this.props}/>;
    }
  }
}

export const GuestRoute = compose(
  withExtractedAuthState,
  withRouter
)(GuestRouteComponent) as React.ComponentClass<ParentProps>;
