import * as React from 'react';
import { Progress } from 'antd';

interface ParentProps {
  color: string;
  width?: number;
  strokeWidth: number;
  rating: number;
}

export const ReputationBreakdownProgressBar: React.SFC<ParentProps> = (props) => {
  const { color , width, strokeWidth, rating } = props;

  return (
    <Progress
      type="circle"
      showInfo={false}
      strokeColor={color}
      percent={rating}
      width={width}
      strokeWidth={strokeWidth}
    />
  );
};
