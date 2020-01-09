import * as React from 'react';
import { getAuthToken } from '../utils/asyncStorageUtil';
const jwt_decode = require('jwt-decode');

export interface WithUserIdProps {
  userId: number
}

interface Props {
  children?: React.ReactNode;
}

interface State {
  userId: number | null;
}

export const withUserId = <P extends Props>(
  WrappedComponent: React.ComponentType<P> | React.StatelessComponent<P>
) => {
  return class extends React.PureComponent<P, State> {
    state: State = {
      userId: null
    };

    async componentDidMount () {
      const token = await getAuthToken();

      if (!token) {
        return;
      }

      const tokenBody = jwt_decode(token);

      // tslint:disable no-string-literal
      const userId = tokenBody['userId']['id'];
      // tslint:enable no-string-literal

      this.setState(ss => ({...ss, userId}));
    }

    render () {
      return <WrappedComponent {...this.props} {...this.state}/>
    }
  }
};