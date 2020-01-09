import * as React from 'react';
import { compose } from 'recompose';
import { withState, WithStateProps } from '@narrative/shared';

/* ****************************************************************************
Modal Context: manages visibility state for any top level modal
******************************************************************************* */

// tslint:disable no-any
export const ModalContext: React.Context<any> = React.createContext(undefined as any);
// tslint:enable no-any

export enum ModalName {
  login = 'login',
}

export interface ModalValueProps {
  isVisible: boolean;
}

export interface ModalActionProps {
  updateModalVisibility: (modalName: string) => void;
}

interface ModalStoreState {
  [ModalName.login]: ModalValueProps;
}

const initialState: ModalStoreState = {
  [ModalName.login]: {
    isVisible: false
  }
};

type TabContextProps =
  WithStateProps<ModalStoreState> &
  ModalActionProps;

const ModalProviderComponent: React.SFC<TabContextProps> = (props) => {
  const { state, setState, children } = props;

  return (
    <ModalContext.Provider value={{
      [ModalName.login]: {
        isVisible: state.login.isVisible
      },
      updateModalVisibility: (modalName: ModalName) =>
        setState(ss => ({
          ...ss,
          [modalName]: { isVisible: !ss[modalName].isVisible }
        }))
    }}>
      {children}
    </ModalContext.Provider>
  );
};

type ModalProviderProps =
  ModalStoreState &
  ModalActionProps;

export const ModalProvider = compose(
  withState<ModalStoreState>(initialState),
)(ModalProviderComponent) as React.ComponentClass<{}>;

/****************
  Modal Store HOC
****************** */

export interface ModalStoreProps {
  modalStoreValues: ModalValueProps;
  modalStoreActions: ModalActionProps;
}

export const ModalConnect = (modalName: ModalName) => {
  return <P extends ModalStoreProps>(
    WrappedComponent: React.ComponentType<P> | React.SFC<P>
  ) => {
    return class extends React.PureComponent {
      public render () {
        return (
          <ModalContext.Consumer>
            {({updateModalVisibility, ...rest}: ModalProviderProps) => {
              const values = rest[modalName];

              return (
                <WrappedComponent
                  modalStoreValues={values}
                  modalStoreActions={{updateModalVisibility}}
                  {...this.props}
                />
              );
            }}
          </ModalContext.Consumer>
        );
      }
    };
  };
};
