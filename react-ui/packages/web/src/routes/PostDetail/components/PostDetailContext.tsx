import * as React from 'react';
import { WithPostByIdProps } from '@narrative/shared';

export interface WithPostDetailContextProps {
  postByIdProps: WithPostByIdProps;
}

// jw: let's create a context to share the PostDetail between the standard post detail UI and when it gets rendered
//     through the Publication.
// tslint:disable-next-line no-any
export const PostDetailContext: React.Context<any> = React.createContext(undefined as any);

export const PostDetailConnect = <P extends WithPostDetailContextProps>(
  WrappedComponent: React.ComponentType<P> | React.SFC<P>
) => {
  return class extends React.PureComponent {
    public render () {
      return (
        <PostDetailContext.Consumer>
          {(contextProps: WithPostDetailContextProps) => {
            return (
              <WrappedComponent
                {...this.props}
                // jw: let's spread the contexts properties after the components own props to ensure they take priority
                {...contextProps}
              />
            );
          }}
        </PostDetailContext.Consumer>
      );
    }
  };
};
