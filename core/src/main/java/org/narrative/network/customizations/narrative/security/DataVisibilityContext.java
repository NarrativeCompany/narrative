package org.narrative.network.customizations.narrative.security;

import org.narrative.network.core.rating.AgeRating;
import org.narrative.network.customizations.narrative.service.api.model.filter.QualityFilter;
import lombok.Value;

import java.util.Set;

/**
 * DTO representing data visibility for use in controllers/services.
 */
@Value
public class DataVisibilityContext {
    private final Set<AgeRating> preferredAgeRatingSet;
    private final Set<AgeRating> permittedAgeRatingSet;
    private final boolean restrictedContentFiltered;
    private final QualityFilter contentQualityFilter;

    public DataVisibilityContext(Set<AgeRating> preferredAgeRatingSet, Set<AgeRating> permittedAgeRatingSet, QualityFilter contentQualityFilter) {
        this.preferredAgeRatingSet = preferredAgeRatingSet;
        this.permittedAgeRatingSet = permittedAgeRatingSet;
        // Use the user's preferred age rating set for filtering
        this.restrictedContentFiltered = !AgeRating.ageRatingsContainRestricted(preferredAgeRatingSet);
        // Use the user's content quality filter setting if available
        this.contentQualityFilter = contentQualityFilter;
    }
}
