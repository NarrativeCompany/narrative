import { FormattedMessage } from 'react-intl';
import { QualityFilter } from '@narrative/shared';
import { MemberPersonalSettingsMessages } from '../i18n/MemberPersonalSettingsMessages';
import { EnumEnhancer } from '../utils/enhancedEnumUtils';

// jw: let's define the QualityFilterHelper that will provide all the extra helper logic for QualityFilters
export class QualityFilterHelper {
  qualityFilter: QualityFilter;
  titleMessage: FormattedMessage.MessageDescriptor;

  constructor(
    qualityFilter: QualityFilter,
    titleMessage: FormattedMessage.MessageDescriptor
  ) {
    this.qualityFilter = qualityFilter;
    this.titleMessage = titleMessage;
  }
}

// jw: next: lets create the lookup of QualityFilter to helper object

const qualityFilterHelpers: {[key: number]: QualityFilterHelper} = [];
// jw: make sure to register these in the order you want them to display.
qualityFilterHelpers[QualityFilter.ONLY_HIGH_QUALITY] = new QualityFilterHelper(
  QualityFilter.ONLY_HIGH_QUALITY,
  MemberPersonalSettingsMessages.QualityLimitTopQualityOptionDescription
);
qualityFilterHelpers[QualityFilter.HIDE_LOW_QUALITY] = new QualityFilterHelper(
  QualityFilter.HIDE_LOW_QUALITY,
  MemberPersonalSettingsMessages.QualityLimitAverageQualityOptionDescription
);
qualityFilterHelpers[QualityFilter.ANY_QUALITY] = new QualityFilterHelper(
  QualityFilter.ANY_QUALITY,
  MemberPersonalSettingsMessages.QualityAnyQualityOptionDescription
);

// jw: finally, let's create the enhancer, which will allow us to lookup helpers by enum instance.
export const EnhancedQualityFilter = new EnumEnhancer<QualityFilter, QualityFilterHelper>(
  qualityFilterHelpers
);
