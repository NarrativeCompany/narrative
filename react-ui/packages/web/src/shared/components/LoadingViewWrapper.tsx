import * as React from 'react';
import { ViewWrapper, ViewWrapperProps } from './ViewWrapper';
import { Loading, LoadingProps } from './Loading';

type Props = LoadingProps &
  Pick<ViewWrapperProps, 'gradientBox'>;

export const LoadingViewWrapper: React.SFC<Props> = (props) => {
  const { gradientBox, ...loadingProps } = props;

  return (
    <ViewWrapper gradientBox={gradientBox}>
      <Loading {...loadingProps} />
     </ViewWrapper>
  );
};
