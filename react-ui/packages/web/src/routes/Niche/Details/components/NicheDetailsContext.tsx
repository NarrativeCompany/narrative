import * as React from 'react';
import { NicheDetail } from '@narrative/shared';

export interface WithNicheDetailsContextProps {
  nicheDetail: NicheDetail;
}

// jw: let's create a context to share the NicheDetail for all niche detail pages. We will be using a connector
//     to share that more easily.
// tslint:disable-next-line no-any
export const NicheDetailsContext: React.Context<any> = React.createContext(undefined as any);

// jw: since we have the user in context, let's provide a Connector to make consuming that user much easier
export const NicheDetailsConnect = <P extends WithNicheDetailsContextProps>(
  WrappedComponent: React.ComponentType<P> | React.SFC<P>
) => {
  return class extends React.PureComponent {
    public render () {
      return (
        <NicheDetailsContext.Consumer>
          {(contextProps: WithNicheDetailsContextProps) => {
            return (
              <WrappedComponent
                {...this.props}
                // jw: let's spread the contexts properties after the components own props to ensure that take priority
                {...contextProps}
              />
            );
          }}
        </NicheDetailsContext.Consumer>
      );
    }
  };
};
