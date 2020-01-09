import { FormattedMessage } from 'react-intl';
import { QualityRating } from '@narrative/shared';
import { RatingMessages } from '../i18n/RatingMessages';
import { EnumEnhancer } from '../utils/enhancedEnumUtils';

// jw: let's define the QualityRatingHelper that will provide all the extra helper logic for QualityRatings
export class QualityRatingHelper {
  rating: QualityRating;
  titleMessage: FormattedMessage.MessageDescriptor;

  constructor(
    rating: QualityRating,
    titleMessage: FormattedMessage.MessageDescriptor
  ) {
    this.rating = rating;
    this.titleMessage = titleMessage;
  }

  isLke() {
    return this.rating === QualityRating.LIKE;
  }

  isDislikeReason() {
    return this.rating !== QualityRating.LIKE;
  }
}

// jw: next: lets create the lookup of QualityRating to helper object

const qualityRatingHelpers: {[key: number]: QualityRatingHelper} = [];
// jw: make sure to register these in the order you want them to display.
qualityRatingHelpers[QualityRating.LIKE] = new QualityRatingHelper(
  QualityRating.LIKE,
  RatingMessages.LikeContent
);
qualityRatingHelpers[QualityRating.DISLIKE_LOW_QUALITY_CONTENT] = new QualityRatingHelper(
  QualityRating.DISLIKE_LOW_QUALITY_CONTENT,
  RatingMessages.LowQualityContent
);
qualityRatingHelpers[QualityRating.DISLIKE_DISAGREE_WITH_VIEWPOINT] = new QualityRatingHelper(
  QualityRating.DISLIKE_DISAGREE_WITH_VIEWPOINT,
  RatingMessages.DisagreeWithViewpoint
);
qualityRatingHelpers[QualityRating.DISLIKE_CONTENT_VIOLATES_AUP] = new QualityRatingHelper(
  QualityRating.DISLIKE_CONTENT_VIOLATES_AUP,
  RatingMessages.ContentViolatesAup
);

// jw: finally, let's create the enhancer, which will allow us to lookup helpers by enum instance.
export const EnhancedQualityRating = new EnumEnhancer<QualityRating, QualityRatingHelper>(
  qualityRatingHelpers
);
