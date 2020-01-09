import { QualityLevel } from '@narrative/shared';
import { EnumEnhancer } from '../utils/enhancedEnumUtils';
import { IconType } from '../components/CustomIcon';
import { FormattedMessage } from 'react-intl';
import { SharedComponentMessages } from '../i18n/SharedComponentMessages';
import { ThemeColorType } from '../styled/theme';
import { TextColor } from '../components/Text';

export class QualityLevelHelper {
  qualityLevel: QualityLevel;
  iconType?: IconType;
  tooltip?: FormattedMessage.MessageDescriptor;
  themeColor: ThemeColorType;
  textColor: TextColor;

  constructor(
    qualityLevel: QualityLevel,
    themeColor: ThemeColorType,
    textColor: TextColor,
    iconType?: IconType,
    tooltip?: FormattedMessage.MessageDescriptor
  ) {
    this.qualityLevel = qualityLevel;
    this.iconType = iconType;
    this.tooltip = tooltip;
    this.themeColor = themeColor;
    this.textColor = textColor;
  }

  isLow() {
    return this.qualityLevel === QualityLevel.LOW;
  }

  isMedium() {
    return this.qualityLevel === QualityLevel.MEDIUM;
  }

  isHigh() {
    return this.qualityLevel === QualityLevel.HIGH;
  }
}

const QualityLevelHelpers: {[key: number]: QualityLevelHelper} = [];

QualityLevelHelpers[QualityLevel.LOW] = new QualityLevelHelper(
  QualityLevel.LOW,
  'primaryRed',
  'error',
  'lowQuality',
  SharedComponentMessages.LowQualityIconTooltip
);
QualityLevelHelpers[QualityLevel.MEDIUM] = new QualityLevelHelper(
  QualityLevel.MEDIUM,
  'secondaryBlue',
  'primary'
);
QualityLevelHelpers[QualityLevel.HIGH] = new QualityLevelHelper(
  QualityLevel.HIGH,
  'primaryBlue',
  'lightBlue',
  'highQuality',
  SharedComponentMessages.HighQualityIconTooltip
);

export const EnhancedQualityLevel = new EnumEnhancer<QualityLevel, QualityLevelHelper>(
  QualityLevelHelpers
);
