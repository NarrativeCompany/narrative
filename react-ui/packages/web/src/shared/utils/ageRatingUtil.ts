import { AgeRating } from '@narrative/shared';

export function resolveAgeRatingFromRestrictedBool(restricted: boolean): AgeRating {
  return restricted ? AgeRating.RESTRICTED : AgeRating.GENERAL;
}

export function resolveRestrictedBoolFromAgeRating(ageRating: AgeRating): boolean {
  return ageRating === AgeRating.RESTRICTED;
}

export function isRestrictedPermittedForCurUser(userAgeRatings: AgeRating[]): boolean {
  return userAgeRatings &&
         userAgeRatings.some(rating => rating === AgeRating.RESTRICTED);
}
