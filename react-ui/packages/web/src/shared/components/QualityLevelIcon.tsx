import * as React from 'react';
import { EnhancedQualityLevel } from '../enhancedEnums/qualityLevel';
import { CustomIcon, IconSize } from './CustomIcon';
import { QualityLevel } from '@narrative/shared';
import { FormattedMessage } from 'react-intl';
import { Tooltip } from 'antd';

export interface QualityLevelIconProps {
  qualityLevel: QualityLevel | null;
  size?: IconSize;
  style?: React.CSSProperties;
}

type Props =
  QualityLevelIconProps;

export const QualityLevelIcon: React.SFC<Props> = (props) => {
  const { qualityLevel, style, size } = props;

  if (!qualityLevel) {
    return null;
  }

  const iconSize = size || 'sm';

  const quality = EnhancedQualityLevel.get(qualityLevel);
  if (quality.iconType && quality.tooltip) {
    return (
      <Tooltip title={<FormattedMessage {...quality.tooltip}/>}>
        <CustomIcon size={iconSize} type={quality.iconType} style={style}/>
      </Tooltip>
    );
  }

  return ( null );
};
