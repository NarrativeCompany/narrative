import * as React from 'react';
import { compose, withProps } from 'recompose';
import { ProgressProps } from 'antd/lib/progress';
import { Progress as AntProgress } from 'antd';
import { themeColors } from '../../../shared/styled/theme';
import styled from '../../../shared/styled/index';

const Progress = styled<ProgressProps & {bgColor: ProgressInnerBg}>(({bgColor, ...props}) => <AntProgress {...props}/>)`
  .ant-progress-inner {
    top: -2px;
    
    ${props => props.bgColor && `background-color: ${props.bgColor}`}
  }
  
  .ant-progress-inner,
  .ant-progress-bg {
    border-radius: 2px 2px 0 0 !important;
  }
`;

type ProgressInnerBg = 'default' | 'error' | 'primary';

interface WithProps {
  bgColor: ProgressInnerBg;
}

interface ParentProps {
  percent: number;
  strokeColor?: string;
  progressInnerBg?: ProgressInnerBg;
}

export type Props =
  ParentProps &
  WithProps;

export const NicheCardProgressBarComponent: React.SFC<Props> = (props) => {
  const { percent, strokeColor, bgColor } = props;

  return (
    <Progress
      showInfo={false}
      percent={percent}
      strokeWidth={25}
      strokeColor={strokeColor}
      bgColor={bgColor}
    />
  );
};

function getProgressBgColor (bgColor?: ProgressInnerBg): string | undefined {
  switch (bgColor) {
    case 'primary':
      return themeColors.secondaryBlue;
    case 'error':
      return themeColors.primaryRed;
    default:
      return undefined;
  }
}

export const NicheCardProgressBar = compose(
  withProps((props: Props) => ({
    bgColor: getProgressBgColor(props.progressInnerBg)
  }))
)(NicheCardProgressBarComponent) as React.ComponentClass<ParentProps>;
