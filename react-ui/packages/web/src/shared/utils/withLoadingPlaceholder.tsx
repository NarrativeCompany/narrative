import * as React from 'react';
import { Loading, ContainedLoading } from '../components/Loading';
import { Card } from '../components/Card';
import { LoadingViewWrapper } from '../components/LoadingViewWrapper';

export interface LoadingProps {
  loading: boolean;
}

export interface WithLoadingPlaceholderProps extends LoadingProps {
  // jw: this needs to be resolved through intl, so we need to get it through the properties.
  loadingPlaceholderTip?: string;
}

// jw: let's define the factory for generating a placeholder.
type LoadingComponentFactory = (tip?: string) => React.ReactNode;

// jw: these are common placeholders that we use in a lot of our UIs.
export const cardPlaceholder: LoadingComponentFactory = () => <Card loading={true} />;
export const fullPlaceholder: LoadingComponentFactory = (tip?: string) => <Loading tip={tip} />;
export const viewWrapperPlaceholder = (gradientBodyFactory?: () => React.ReactNode): LoadingComponentFactory => {
  return (tip?: string) => <LoadingViewWrapper tip={tip} gradientBox={gradientBodyFactory && gradientBodyFactory()} />;
};

// jw: finally, the HOC generation function. We are using this so that you can more naturally specify a custom indicator
//     factory if needed.
export const withLoadingPlaceholder = (
  loadingIndicatorFactory?: LoadingComponentFactory
) => {
  return <P extends WithLoadingPlaceholderProps>(
    WrappedComponent: React.ComponentType<P> | React.SFC<P>
  ) => {
    return class extends React.PureComponent<P> {
      public render () {
        const { loading, loadingPlaceholderTip } = this.props;

        // jw: if we have a loading indicator, we are not loading then render the base component.
        if (loading !== null && !loading) {
          return <WrappedComponent {...this.props} />;
        }

        // jw: if we have a custom indicator factory then let's use that
        if (loadingIndicatorFactory) {
          return loadingIndicatorFactory(loadingPlaceholderTip);
        }

        // jw: otherwise, let's default to the contained loading.
        return <ContainedLoading tip={loadingPlaceholderTip} />;
      }
    };
  };
};
