import * as React from 'react';
import { DetailsGradientBox, DetailsGradientBoxColor } from './DetailsGradientBox';
import { SidebarViewWrapper, SidebarViewWrapperProps } from './SidebarViewWrapper';
import { Omit } from 'recompose';

export interface DetailsViewWrapperProps extends Omit<SidebarViewWrapperProps, 'headerContent' | 'gradientBox'> {
  gradientBoxColor?: DetailsGradientBoxColor;
}

export const DetailsViewWrapper: React.SFC<DetailsViewWrapperProps> = (props) => {
  const { gradientBoxColor, ...viewWrapperProps } = props;

  return (
    <SidebarViewWrapper
      {...viewWrapperProps}
      gradientBox={gradientBoxColor ? <DetailsGradientBox color={gradientBoxColor}/> : undefined}
    />
  );
};
