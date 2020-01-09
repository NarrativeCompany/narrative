package org.narrative.network.customizations.narrative.security;

import org.narrative.network.core.rating.AgeRating;
import org.narrative.network.customizations.narrative.service.api.model.filter.QualityFilter;

import java.util.Set;

/**
 * Helper that manages access to a {@link ThreadLocal} {@link DataVisibilityContext} in the case of web requests and falls back to
 * a default configuration for non-web requests.
 */
public class DataVisibilityContextManager {
    /**
     * This thread local is managed by {@link DataVisibilityFilter}
     */
    private static final ThreadLocal<DataVisibilityContext> requestDataVisibilityContextThreadLocal = new ThreadLocal<>();
    /**
     * Build the default visibility (unrestricted - service invocations will use this)
     */
    private static final DataVisibilityContext defaultDataVisibilityContext =
            new DataVisibilityContext(AgeRating.ALL_AGE_RATINGS, AgeRating.ALL_AGE_RATINGS, QualityFilter.DEFAULT_FILTER);

    static void setDataVisibilityContext(DataVisibilityContext dataVisibilityContext) {
        requestDataVisibilityContextThreadLocal.set(dataVisibilityContext);
    }

    static void clearDataVisibilityContext() {
        requestDataVisibilityContextThreadLocal.remove();
    }

    public static DataVisibilityContext getDataVisibilityContext() {
        DataVisibilityContext res = requestDataVisibilityContextThreadLocal.get();
        return res != null ? res : defaultDataVisibilityContext;
    }

    public static Set<AgeRating> getPermittedAgeRatingSet() {
        // Use the user's preferred age rating set for filtering
        return getDataVisibilityContext().getPreferredAgeRatingSet();
    }

    private static Set<AgeRating> getUserPermittedAgeRatingSet() {
        return getDataVisibilityContext().getPermittedAgeRatingSet();
    }

    public static boolean isRestrictedContentFiltered() {
        // This is also derived from the user's preferred age rating set for filtering
        return getDataVisibilityContext().isRestrictedContentFiltered();
    }

    /**
     * Validate that the current user (if in a request) has access for the passed age rating.
     *
     * @param ageRating  The age rating to test
     * @param messageKey The message key to use for the resulting exception if validation fails
     * @throws DataVisibilityAccessViolation when the age rating is not allowed for the current user (if in a request)
     */
    public static void validateVisibilityForAgeRating(AgeRating ageRating, String titleKey, String messageKey) {
        // Use the user's full permitted age rating set here - the user may want access to a resource that they are
        // permitted to see but is normally filtered due to their preferences (i.e. via direct URL).
        if (!getUserPermittedAgeRatingSet().contains(ageRating)) {
            throw new DataVisibilityAccessViolation(titleKey, messageKey);
        }
    }

    public static QualityFilter getContentQualityFilter() {
        return getDataVisibilityContext().getContentQualityFilter();
    }
}
