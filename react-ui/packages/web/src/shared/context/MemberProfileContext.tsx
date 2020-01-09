import * as React from 'react';
import { UserDetail } from '@narrative/shared';

// jw: this will be used to store values into the context, but also to share those with children components.
export interface WithMemberProfileProps {
  detailsForProfile: UserDetail;
  isForCurrentUser: boolean;
}

// jw: let's create a context to share the UserDetail for the profile with all children. We will be using a connector
//     to share that more easily, so no need to make this public
// tslint:disable-next-line no-any
export const MemberProfileContext: React.Context<any> = React.createContext(undefined as any);

// jw: since we have the user in context, let's provide a Connector to make consuming that user much easier
export const MemberProfileConnect = <P extends WithMemberProfileProps>(
  WrappedComponent: React.ComponentType<P> | React.SFC<P>
) => {
  return class extends React.PureComponent {
    public render () {
      return (
        <MemberProfileContext.Consumer>
          {(contextProps: WithMemberProfileProps) => {
            return (
              <WrappedComponent
                {...contextProps}
                {...this.props}
              />
            );
          }}
        </MemberProfileContext.Consumer>
      );
    }
  };
};
