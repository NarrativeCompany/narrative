import * as React from 'react';
import { ModalProvider } from './ModalStore';
import { AuthProvider } from './AuthStore';
import { LayoutBgProvider } from './LayoutBgStore';

type Store = React.ComponentType<{}> | React.SFC<{}>;

interface UIStateStoreProps {
  modal: Store;
  auth: Store;
  layoutBg: Store;
}

export const stores: UIStateStoreProps = {
  modal: ModalProvider,
  auth: AuthProvider,
  layoutBg: LayoutBgProvider
};

interface UIStateStore {
  store: UIStateStoreProps;
}

export const UIState: React.SFC<UIStateStore> = ({store, children}) => {

  return (
    <React.Fragment>
      {Object.values(store).reduce(
        (tree, Provider) => (
          <Provider>{tree}</Provider>
        ),
        children
      )}
    </React.Fragment>
  );
};
