import { FormattedMessage } from 'react-intl';
import { AgeRating } from '@narrative/shared';
import { RatingMessages } from '../i18n/RatingMessages';
import { EnumEnhancer } from '../utils/enhancedEnumUtils';
import { ThemeColorType } from '../styled/theme';

// jw: let's define the AgeRatingHelper that will provide all the extra helper logic for AgeRatings
export class AgeRatingHelper {
  rating: AgeRating;
  titleMessage: FormattedMessage.MessageDescriptor;
  themeColor: ThemeColorType;

  constructor(
    rating: AgeRating,
    titleMessage: FormattedMessage.MessageDescriptor,
    themeColor: ThemeColorType
  ) {
    this.rating = rating;
    this.titleMessage = titleMessage;
    this.themeColor = themeColor;
  }

  isGeneral() {
    return this.rating === AgeRating.GENERAL;
  }

  isRestricted() {
    return this.rating === AgeRating.RESTRICTED;
  }
}

// jw: next: lets create the lookup of AgeRating to helper object

const ageRatingHelpers: {[key: number]: AgeRatingHelper} = [];
// jw: make sure to register these in the order you want them to display.
ageRatingHelpers[AgeRating.GENERAL] = new AgeRatingHelper(
  AgeRating.GENERAL,
  RatingMessages.AllAges,
  'secondaryBlue'
);
ageRatingHelpers[AgeRating.RESTRICTED] = new AgeRatingHelper(
  AgeRating.RESTRICTED,
  RatingMessages.EighteenPlus,
  'primaryRed'
);

// jw: finally, let's create the enhancer, which will allow us to lookup helpers by enum instance.
export const EnhancedAgeRating = new EnumEnhancer<AgeRating, AgeRatingHelper>(
  ageRatingHelpers
);
