package org.narrative.network.customizations.narrative.serialization.jackson.view;

import com.fasterxml.jackson.annotation.JsonView;

/**
 * Jackson {@link JsonView}s.
 */
public class View {
    public static class Summary {}
    public static class Detailed extends Summary {}

    /**
     * Resolve the view class from a String representing the view name.
     *
     * @param viewName The view name to resolve
     * @return The view class if found, otherwise {@link Summary}
     */
    public static Class resolveView(String viewName) {
        Class res = Summary.class;
        if (Detailed.class.getSimpleName().equalsIgnoreCase(viewName)) {
            res = Detailed.class;
        }

        return res;
    }
}
