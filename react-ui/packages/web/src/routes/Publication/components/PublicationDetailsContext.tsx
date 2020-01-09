import * as React from 'react';
import { PublicationDetail } from '@narrative/shared';
import { PublicationRoleBooleans } from '../../../shared/utils/publicationRoleUtils';

export interface WithPublicationDetailsContextProps {
  publicationDetail: PublicationDetail;
  currentUserRoles: PublicationRoleBooleans;
  refetchPublicationDetail?: () => void;
}

// jw: let's create a context to share the PublicationDetail for all publication detail pages. We will be using a
//     connector to share that more easily.
// tslint:disable-next-line no-any
export const PublicationDetailsContext: React.Context<any> = React.createContext(undefined as any);

// jw: since we have the user in context, let's provide a Connector to make consuming that user much easier
export const PublicationDetailsConnect = <P extends WithPublicationDetailsContextProps>(
  WrappedComponent: React.ComponentType<P> | React.SFC<P>
) => {
  return class extends React.PureComponent {
    public render () {
      return (
        <PublicationDetailsContext.Consumer>
          {(contextProps: WithPublicationDetailsContextProps) => {
            return (
              <WrappedComponent
                {...this.props}
                // jw: let's spread the contexts properties after the components own props to ensure they take priority
                {...contextProps}
              />
            );
          }}
        </PublicationDetailsContext.Consumer>
      );
    }
  };
};
