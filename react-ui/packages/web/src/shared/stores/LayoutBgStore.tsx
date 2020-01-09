import * as React from 'react';
import { compose } from 'recompose';
import { withState, WithStateProps } from '@narrative/shared';
import { themeLayout } from '../styled/theme';

// tslint:disable-next-line no-any
export const LayoutBgContext: React.Context<any> = React.createContext(undefined as any);

interface LayoutBgActionProps {
  changeLayout: (bg?: LayoutBg, headerBg?: HeaderBg, showHeaderNavOnHover?: boolean) => void;
  changeLayoutBg: (bg?: LayoutBg) => void;
  changeLayoutHeaderBg: (headerBg?: HeaderBg) => void;
}

export type LayoutBg = keyof typeof themeLayout.layoutBackground;
export type HeaderBg = keyof typeof themeLayout.headerBackground;

interface LayoutBgValueState {
  layoutBg: LayoutBg;
  headerBg: HeaderBg;
  showHeaderNavOnHover?: boolean;
}

const initialState: LayoutBgValueState = {
  layoutBg: 'gray',
  headerBg: 'white',
};

type LayoutBgContextProps =
  WithStateProps<LayoutBgValueState>;

const LayoutBgProviderComponent: React.SFC<LayoutBgContextProps> = (props) => {
  const { state, setState, children } = props;

  return (
    <LayoutBgContext.Provider value={{
      layoutBg: state.layoutBg,
      headerBg: state.headerBg,
      showHeaderNavOnHover: state.showHeaderNavOnHover,
      changeLayout: (bg?: LayoutBg, hdrBg?: HeaderBg, showHeaderNavOnHover?: boolean) => {
        const layoutBg = bg || initialState.layoutBg;
        const headerBg = hdrBg || initialState.headerBg;
        setState(ss => ({...ss, layoutBg, headerBg, showHeaderNavOnHover}));
      },
      changeLayoutBg: (bg?: LayoutBg) => {
        const layoutBg = bg || initialState.layoutBg;
        setState(ss => ({...ss, layoutBg}));
      },
      changeLayoutHeaderBg: (hdrBg?: HeaderBg) => {
        const headerBg = hdrBg || initialState.headerBg;
        setState(ss => ({...ss, headerBg}));
      },
    }}>
      {children}
    </LayoutBgContext.Provider>
  );
};

type LayoutBgProviderProps =
  LayoutBgValueState &
  LayoutBgActionProps;

export const LayoutBgProvider = compose(
  withState<LayoutBgValueState>(initialState),
)(LayoutBgProviderComponent) as React.ComponentClass<{}>;

export type LayoutBgStoreProps = LayoutBgProviderProps;

export const LayoutBgConnect = <P extends LayoutBgStoreProps>(
  WrappedComponent: React.ComponentType<P> | React.SFC<P>
) => {
  return class extends React.PureComponent {
    public render () {
      return (
        <LayoutBgContext.Consumer>
          {({ layoutBg, headerBg, showHeaderNavOnHover, changeLayout, changeLayoutBg, changeLayoutHeaderBg }
          : LayoutBgProviderProps) => {
            return (
              <WrappedComponent
                layoutBg={layoutBg}
                headerBg={headerBg}
                showHeaderNavOnHover={showHeaderNavOnHover}
                changeLayout={changeLayout}
                changeLayoutBg={changeLayoutBg}
                changeLayoutHeaderBg={changeLayoutHeaderBg}
                {...this.props}
              />
            );
          }}
        </LayoutBgContext.Consumer>
      );
    }
  };
};
