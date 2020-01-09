import { FormattedMessage } from 'react-intl';
import { FeaturePostDuration } from '@narrative/shared';
import { EnumEnhancer } from '../utils/enhancedEnumUtils';
import { PublicationDetailsMessages } from '../i18n/PublicationDetailsMessages';

// jw: let's define the FeaturePostDurationHelper that will provide all the extra helper logic for FeaturePostDurations
export class FeaturePostDurationHelper {
  duration: FeaturePostDuration;
  title: FormattedMessage.MessageDescriptor;
  titleLc: FormattedMessage.MessageDescriptor;

  constructor(
    duration: FeaturePostDuration,
    title: FormattedMessage.MessageDescriptor,
    titleLc: FormattedMessage.MessageDescriptor
  ) {
    this.duration = duration;
    this.title = title;
    this.titleLc = titleLc;
  }
}

// jw: next: lets create the lookup of FeaturePostDuration to helper object

const helpers: {[key: number]: FeaturePostDurationHelper} = [];
// jw: make sure to register these in the order you want them to display.
helpers[FeaturePostDuration.ONE_DAY] = new FeaturePostDurationHelper(
  FeaturePostDuration.ONE_DAY,
  PublicationDetailsMessages.OneDay,
  PublicationDetailsMessages.OneDayLc
);
helpers[FeaturePostDuration.THREE_DAYS] = new FeaturePostDurationHelper(
  FeaturePostDuration.THREE_DAYS,
  PublicationDetailsMessages.ThreeDays,
  PublicationDetailsMessages.ThreeDaysLc
);
helpers[FeaturePostDuration.ONE_WEEK] = new FeaturePostDurationHelper(
  FeaturePostDuration.ONE_WEEK,
  PublicationDetailsMessages.OneWeek,
  PublicationDetailsMessages.OneWeekLc
);

// jw: finally, let's create the enhancer, which will allow us to lookup helpers by enum instance.
export const EnhancedFeaturePostDuration = new EnumEnhancer<FeaturePostDuration, FeaturePostDurationHelper>(
  helpers
);
