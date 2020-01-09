import * as React from 'react';
import { Redirect, Route, RouteComponentProps, RouteProps } from 'react-router-native';
import { AppRoutes } from '../constants/routes';
import { compose } from 'recompose';
import { withUserId, WithUserIdProps } from '../containers/withUserId';

// interface State {
//   token: string | null;
//   isMounted: boolean;
// }

type ParentProps = RouteProps;

type Props =
  ParentProps &
  WithUserIdProps;

// class AuthRouteComponent extends React.PureComponent<Props, State> {
//   constructor (props: Props) {
//     super(props);
//
//     this.state = {
//       token: null,
//       isMounted: false
//     }
//   }
//
//   async componentWillMount () {
//     // this.setState(ss => ({...ss, isMounted: true}));
//     const token = await getAuthToken();
//
//     if (!token) {
//       return;
//     }
//
//     this.setState({token});
//   }
//
//   componentWillUnmount () {
//     // this.setState(ss => ({...ss, isMounted: false}));
//   }
//
//   render () {
//     if (!this.state.token) {
//       return <Redirect to={AppRoutes.Login}/>
//     }
//
//     return <Route {...this.props}/>
//   }
// }

const AuthRouteComponent: React.SFC<Props> = (props) => {
  const { userId, component, ...rest } = props;

  const renderRoute = (routeProps: RouteComponentProps<{}>) => {
    if (!userId) {
      return <Redirect to={AppRoutes.Login}/>;
    }

    const Component = component as any;

    return <Component {...routeProps}/>;
  };

  return <Route{...rest} render={renderRoute}/>;
};

export const AuthRoute = compose<Props, {}>(
  withUserId
)(AuthRouteComponent) as React.ComponentClass<ParentProps>;
